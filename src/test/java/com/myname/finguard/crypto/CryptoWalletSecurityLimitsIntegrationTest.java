package com.myname.finguard.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.MailService;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.crypto.wallet.max-per-user=1",
        "app.security.rate-limit.wallets.list.limit=1",
        "app.security.rate-limit.wallets.list.window-ms=600000",
        "app.security.rate-limit.wallets.create.limit=100",
        "app.security.rate-limit.wallets.create.window-ms=600000",
        "app.security.rate-limit.wallets.delete.limit=100",
        "app.security.rate-limit.wallets.delete.window-ms=600000"
})
@Transactional
class CryptoWalletSecurityLimitsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @MockBean
    private CryptoRatesProvider cryptoRatesProvider;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
        // avoid any accidental external calls during list
        org.mockito.Mockito.when(cryptoRatesProvider.fetchLatest(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of()
                ));
    }

    @Test
    void enforcesMaxWalletsPerUser() throws Exception {
        String email = "wallet-limit-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");

        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ETH","address":"0xabcdefabcdefabcdefabcdefabcdefabcdefabcd","label":"One"}
                                """))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ETH","address":"0x1111111111111111111111111111111111111111","label":"Two"}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
        assertThat(error.get("message").asText()).containsIgnoringCase("wallet limit");
    }

    @Test
    void rateLimitsWalletListPerUser() throws Exception {
        String email = "wallet-list-rl-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");

        mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.RATE_LIMIT);
    }

    private String registerVerifyAndLogin(String email, String password, String baseCurrency) throws Exception {
        String registerPayload = """
                {
                  "email": "%s",
                  "password": "%s",
                  "fullName": "Test User",
                  "baseCurrency": "%s"
                }
                """.formatted(email, password, baseCurrency);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String verifyToken = extractCode(msg.body());
        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, verifyToken)))
                .andExpect(status().isOk());

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginNode = objectMapper.readTree(loginResponse);
        return loginNode.get("token").asText();
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        Matcher tokenParam = Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return URLDecoder.decode(tokenParam.group(1), java.nio.charset.StandardCharsets.UTF_8);
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
}
