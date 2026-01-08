package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.service.MailService;
import jakarta.servlet.http.Cookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.csrf.enabled=true",
        "app.security.jwt.cookie-secure=false"
})
class CryptoWalletCsrfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    @Transactional
    void postAndDeleteRequireXsrfHeaderWhenCookiePresent() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "csrf-wallet-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);

        MvcResult verified = verify(email, extractLatestCode(), csrf);
        Cookie auth = verified.getResponse().getCookie("FG_AUTH");
        assertThat(auth).isNotNull();

        // Create wallet without header -> forbidden
        MvcResult blockedCreate = mockMvc.perform(post("/api/crypto/wallets")
                        .cookie(auth)
                        .cookie(csrf.cookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh","label":"Ledger"}
                                """))
                .andExpect(status().isForbidden())
                .andReturn();
        assertAuthCode(blockedCreate, ErrorCodes.AUTH_INVALID_CREDENTIALS);

        // Create wallet with header -> ok
        MvcResult created = mockMvc.perform(post("/api/crypto/wallets")
                        .cookie(auth)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh","label":"Ledger"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long walletId = objectMapper.readTree(created.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();
        assertThat(walletId).isPositive();

        // Delete wallet without header -> forbidden
        MvcResult blockedDelete = mockMvc.perform(delete("/api/crypto/wallets/{id}", walletId)
                        .cookie(auth)
                        .cookie(csrf.cookie()))
                .andExpect(status().isForbidden())
                .andReturn();
        assertAuthCode(blockedDelete, ErrorCodes.AUTH_INVALID_CREDENTIALS);

        // Delete wallet with wrong header -> forbidden
        MvcResult blockedWrong = mockMvc.perform(delete("/api/crypto/wallets/{id}", walletId)
                        .cookie(auth)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token() + "x"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertAuthCode(blockedWrong, ErrorCodes.AUTH_INVALID_CREDENTIALS);

        // Delete wallet with correct header -> no content
        mockMvc.perform(delete("/api/crypto/wallets/{id}", walletId)
                        .cookie(auth)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isNoContent());
    }

    private void assertAuthCode(MvcResult result, String expectedCode) throws Exception {
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode error = objectMapper.readTree(body);
        assertThat(error.get("code").asText()).isEqualTo(expectedCode);
    }

    private void register(String email, String password, Csrf csrf) throws Exception {
        mailService.clearOutbox();
        mockMvc.perform(post("/api/auth/register")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private MvcResult verify(String email, String code, Csrf csrf) throws Exception {
        return mockMvc.perform(post("/api/auth/verify")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Csrf fetchCsrf() throws Exception {
        MvcResult csrfRes = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode csrfBody = objectMapper.readTree(csrfRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = csrfBody.get("token").asText();
        Cookie cookie = csrfRes.getResponse().getCookie("XSRF-TOKEN");

        assertThat(token).isNotBlank();
        assertThat(cookie).isNotNull();
        return new Csrf(token, cookie);
    }

    private String extractLatestCode() {
        if (mailService.getOutbox().isEmpty()) {
            return "";
        }
        String body = mailService.getOutbox().get(mailService.getOutbox().size() - 1).body();
        return extractCode(body);
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        Matcher tokenParam = Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return URLDecoder.decode(tokenParam.group(1), StandardCharsets.UTF_8);
        }
        Matcher codeLine = Pattern.compile("(?i)code:\\s*([A-Za-z0-9-]{6,})").matcher(body);
        if (codeLine.find()) {
            return codeLine.group(1).trim();
        }
        Matcher digits = Pattern.compile("\\b\\d{6}\\b").matcher(body);
        if (digits.find()) {
            return digits.group().trim();
        }
        Matcher hex = Pattern.compile("\\b(?=[0-9a-fA-F-]*\\d)[0-9a-fA-F-]{6,}\\b").matcher(body);
        return hex.find() ? hex.group().trim() : "";
    }

    private record Csrf(String token, Cookie cookie) {
    }
}
