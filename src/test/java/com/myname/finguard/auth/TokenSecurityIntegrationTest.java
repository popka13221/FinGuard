package com.myname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.model.UserToken;
import com.myname.finguard.auth.model.UserTokenType;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.auth.repository.PasswordResetSessionRepository;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
import com.myname.finguard.auth.repository.UserTokenRepository;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.security.RateLimiterService;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.otp.enabled=false",
        "app.security.tokens.reset-cooldown-seconds=0",
        "app.security.tokens.fixed-code=654321"
})
class TokenSecurityIntegrationTest {

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
    private PasswordResetSessionRepository passwordResetSessionRepository;
    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;
    @Autowired
    private RateLimiterService rateLimiterService;

    @AfterEach
    void cleanup() {
        passwordResetSessionRepository.deleteAll();
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        userRepository.deleteAll();
        mailService.clearOutbox();
        rateLimiterService.reset();
    }

    @Test
    void resetTokensStoredHashedOnly() throws Exception {
        String email = "hashtest@" + UUID.randomUUID() + ".com";
        register(email, "StrongPass1!");
        mailService.clearOutbox();

        mockMvc.perform(post("/api/auth/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        MailService.MailMessage msg = latestMail();
        assertThat(msg).isNotNull();
        String code = extractCode(msg.body());
        assertThat(code).isNotBlank();

        User user = userRepository.findByEmail(email).orElseThrow();
        Optional<UserToken> tokenOpt = userTokenRepository.findFirstByUserAndTypeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                user, UserTokenType.RESET, java.time.Instant.now());
        assertThat(tokenOpt).isPresent();
        UserToken token = tokenOpt.get();
        String expectedHash = sha256Hex(code);
        assertThat(token.getTokenHash()).isEqualTo(expectedHash);
        assertThat(token.getTokenHash()).isNotEqualTo(code);
    }

    @Test
    void accessAndRefreshInvalidAfterPasswordReset() throws Exception {
        String email = "version@" + UUID.randomUUID() + ".com";
        String password = "StrongPass1!";
        String newPassword = "NewStrong1!";

        MvcResult register = register(email, password);
        JsonNode regBody = objectMapper.readTree(register.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String accessToken = regBody.get("token").asText();
        Map<String, String> cookies = cookies(register);
        String refreshToken = cookies.get("FG_REFRESH");
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // Access works before reset
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Trigger reset
        mailService.clearOutbox();
        mockMvc.perform(post("/api/auth/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isOk());
        String code = extractCode(latestMail().body());

        // Confirm reset to get session token
        MvcResult confirm = mockMvc.perform(post("/api/auth/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode confirmBody = objectMapper.readTree(confirm.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String resetSessionToken = confirmBody.get("resetSessionToken").asText();
        assertThat(resetSessionToken).isNotBlank();

        // Reset password
        mockMvc.perform(post("/api/auth/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resetSessionToken":"%s","password":"%s"}
                                """.formatted(resetSessionToken, newPassword)))
                .andExpect(status().isOk());

        // Old access/refresh must be rejected
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("FG_REFRESH", refreshToken)))
                .andExpect(status().isUnauthorized());

        // Login with new password works and issues fresh tokens
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, newPassword)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginBody = objectMapper.readTree(login.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(loginBody.get("token").asText()).isNotBlank();
    }

    @Test
    void issuingResetForAnotherUserDoesNotInvalidateExistingResetToken() throws Exception {
        String emailA = "reset-a-" + UUID.randomUUID() + "@example.com";
        String emailB = "reset-b-" + UUID.randomUUID() + "@example.com";
        register(emailA, "StrongPass1!");
        register(emailB, "StrongPass1!");
        mailService.clearOutbox();

        mockMvc.perform(post("/api/auth/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(emailA)))
                .andExpect(status().isOk());

        User userA = userRepository.findByEmail(emailA).orElseThrow();
        Optional<UserToken> tokenA = userTokenRepository.findFirstByUserAndTypeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                userA, UserTokenType.RESET, java.time.Instant.now());
        assertThat(tokenA).isPresent();

        mockMvc.perform(post("/api/auth/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(emailB)))
                .andExpect(status().isOk());

        User userB = userRepository.findByEmail(emailB).orElseThrow();
        Optional<UserToken> tokenB = userTokenRepository.findFirstByUserAndTypeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                userB, UserTokenType.RESET, java.time.Instant.now());
        assertThat(tokenB).isPresent();
        assertThat(tokenB.get().getTokenHash()).isEqualTo(tokenA.get().getTokenHash());

        // user A token must still exist after issuing a token for user B
        Optional<UserToken> tokenAAgain = userTokenRepository.findFirstByUserAndTypeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                userA, UserTokenType.RESET, java.time.Instant.now());
        assertThat(tokenAAgain).isPresent();
        assertThat(tokenAAgain.get().getId()).isEqualTo(tokenA.get().getId());
    }

    private MvcResult register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        String verifyCode = extractCode(latestMail().body());
        return mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, verifyCode)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MailService.MailMessage latestMail() {
        List<MailService.MailMessage> outbox = mailService.getOutbox();
        return outbox.isEmpty() ? null : outbox.get(outbox.size() - 1);
    }

    private Map<String, String> cookies(MvcResult result) {
        return result.getResponse().getCookies() == null
                ? Map.of()
                : java.util.Arrays.stream(result.getResponse().getCookies())
                .collect(java.util.stream.Collectors.toMap(Cookie::getName, Cookie::getValue));
    }

    private String extractCode(String body) {
        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^[0-9a-fA-F-]{6,}$")) {
                return line;
            }
            if (line.toLowerCase().startsWith("код для ввода") || line.toLowerCase().startsWith("код:")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }
        // fallback: grab first numeric word
        for (String line : lines) {
            for (String part : line.split("\\s+")) {
                if (part.matches("^[0-9a-fA-F-]{6,}$")) {
                    return part.trim();
                }
            }
        }
        return "";
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
