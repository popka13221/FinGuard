package com.myname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
import com.myname.finguard.auth.repository.UserTokenRepository;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.security.RateLimiterService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseCurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;
    @Autowired
    private RateLimiterService rateLimiterService;

    @AfterEach
    void cleanup() {
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        userRepository.deleteAll();
        mailService.clearOutbox();
        rateLimiterService.reset();
    }

    @Test
    void canUpdateBaseCurrency() throws Exception {
        String email = "base-" + UUID.randomUUID() + "@example.com";
        String accessToken = registerAndVerify(email);

        String response = mockMvc.perform(patch("/api/auth/me/base-currency")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"EUR"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode updated = objectMapper.readTree(response);
        assertThat(updated.get("baseCurrency").asText()).isEqualTo("EUR");

        String meResponse = mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode me = objectMapper.readTree(meResponse);
        assertThat(me.get("baseCurrency").asText()).isEqualTo("EUR");
    }

    @Test
    void updateBaseCurrencyRejectsUnsupportedCode() throws Exception {
        String email = "base-bad-" + UUID.randomUUID() + "@example.com";
        String accessToken = registerAndVerify(email);

        mockMvc.perform(patch("/api/auth/me/base-currency")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"GBP"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private String registerAndVerify(String email) throws Exception {
        String password = "StrongPass1!";
        mailService.clearOutbox();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        MailService.MailMessage msg = latestMail();
        assertThat(msg).isNotNull();
        String code = extractCode(msg.body());
        assertThat(code).isNotBlank();

        MvcResult verify = mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk())
                .andReturn();

        String verifyBody = verify.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode json = objectMapper.readTree(verifyBody);
        String token = json.get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private MailService.MailMessage latestMail() {
        List<MailService.MailMessage> outbox = mailService.getOutbox();
        return outbox.isEmpty() ? null : outbox.get(outbox.size() - 1);
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        java.util.regex.Matcher tokenParam = java.util.regex.Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return java.net.URLDecoder.decode(tokenParam.group(1), java.nio.charset.StandardCharsets.UTF_8);
        }
        java.util.regex.Matcher digits = java.util.regex.Pattern.compile("\\b\\d{6}\\b").matcher(body);
        return digits.find() ? digits.group().trim() : "";
    }
}

