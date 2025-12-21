package com.myname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.UserTokenRepository;
import com.myname.finguard.auth.repository.PasswordResetSessionRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.security.RateLimiterService;
import com.myname.finguard.security.LoginAttemptService;
import jakarta.servlet.http.Cookie;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
        "app.security.otp.enabled=true",
        "app.security.otp.ttl-seconds=300",
        "app.security.otp.max-attempts=3",
        "app.security.rate-limit.auth.limit=10",
        "app.security.rate-limit.login-email.limit=10",
        "app.security.rate-limit.login-email.window-ms=60000",
        "app.security.rate-limit.login-otp.limit=5",
        "app.security.rate-limit.login-otp.window-ms=60000",
        "app.security.rate-limit.login-otp-issue.limit=1",
        "app.security.rate-limit.login-otp-issue.window-ms=60000",
        "app.security.auth.require-email-verified=true"
})
class OtpAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private PasswordResetSessionRepository passwordResetSessionRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;

    @AfterEach
    void cleanup() {
        passwordResetSessionRepository.deleteAll();
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        userRepository.deleteAll();
        mailService.clearOutbox();
        rateLimiterService.reset();
        loginAttemptService.reset();
    }

    @Test
    void otpFlowRequiresSecondFactor() throws Exception {
        String email = "otp@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        mailService.clearOutbox();

        MvcResult step1 = postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isAccepted())
                .andReturn();
        JsonNode challenge = objectMapper.readTree(step1.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(challenge.get("otpRequired").asBoolean()).isTrue();
        assertThat(challenge.get("expiresInSeconds").asLong()).isGreaterThan(0);

        // OTP sent to mail
        assertThat(mailService.getOutbox()).isNotEmpty();
        MailService.MailMessage last = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String otp = extractCode(last.body());
        assertThat(otp).isNotBlank();

        MvcResult step2 = postJson("/api/auth/login/otp", """
                {"email":"%s","code":"%s"}
                """.formatted(email, otp))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, String> cookies = cookies(step2);
        assertThat(cookies.get("FG_AUTH")).isNotBlank();
        assertThat(cookies.get("FG_REFRESH")).isNotBlank();
    }

    @Test
    void wrongOtpRejected() throws Exception {
        String email = "otp-wrong@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        mailService.clearOutbox();

        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isAccepted());

        // repeated login while OTP живой не шлёт новый код
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isAccepted());
        assertThat(mailService.getOutbox()).hasSize(1);
        String otp = extractCode(mailService.getOutbox().get(0).body());
        assertThat(otp).isNotBlank();

        for (int i = 0; i < 2; i++) {
            postJson("/api/auth/login/otp", """
                    {"email":"%s","code":"000000"}
                    """.formatted(email)).andExpect(status().isUnauthorized());
        }
        // третья попытка тоже неверная и должна инвалировать код
        MvcResult res = postJson("/api/auth/login/otp", """
                {"email":"%s","code":"000000"}
                """.formatted(email))
                .andExpect(status().isUnauthorized())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100001");

        // даже правильный код после превышения попыток не сработает
        MvcResult finalAttempt = postJson("/api/auth/login/otp", """
                {"email":"%s","code":"%s"}
                """.formatted(email, otp))
                .andExpect(status().isUnauthorized())
                .andReturn();
        JsonNode finalError = objectMapper.readTree(finalAttempt.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(finalError.get("code").asText()).isEqualTo("100001");
    }

    @Test
    void otpIssueIsRateLimitedPerIp() throws Exception {
        String email = "otp-ip@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        mailService.clearOutbox();

        // First login issues OTP
        postJsonFromIp("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email), "10.0.0.1").andExpect(status().isAccepted());
        String otp = extractCode(mailService.getOutbox().get(0).body());
        postJsonFromIp("/api/auth/login/otp", """
                {"email":"%s","code":"%s"}
                """.formatted(email, otp), "10.0.0.1").andExpect(status().isOk());

        // Second login immediately from same IP should hit issue limit (email+ip buckets)
        MvcResult limited = postJsonFromIp("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email), "10.0.0.1")
                .andExpect(status().isTooManyRequests())
                .andReturn();
        JsonNode err = objectMapper.readTree(limited.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(err.get("code").asText()).isEqualTo("429002");
        // No extra emails sent
        assertThat(mailService.getOutbox()).hasSize(1);
    }

    @Test
    void repeatedLoginTooSoonIsRateLimited() throws Exception {
        String email = "otp-limit@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        mailService.clearOutbox();

        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isAccepted());
        // consume code successfully
        postJson("/api/auth/login/otp", """
                {"email":"%s","code":"%s"}
                """.formatted(email, extractCode(mailService.getOutbox().get(0).body()))).andExpect(status().isOk());

        // immediate second login should be blocked by issue rate limit
        MvcResult res = postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isTooManyRequests())
                .andReturn();
        JsonNode err = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(err.get("code").asText()).isEqualTo("429002");
        assertThat(err.get("retryAfterSeconds").asLong()).isGreaterThan(0);
        assertThat(mailService.getOutbox()).hasSize(1);
    }

    private MvcResult registerUser(String email, String password) throws Exception {
        String payload = """
                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                """.formatted(email, password);
        MvcResult res = postJson("/api/auth/register", payload).andExpect(status().isCreated()).andReturn();
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setEmailVerified(true);
            userRepository.save(u);
        });
        return res;
    }

    private org.springframework.test.web.servlet.ResultActions postJson(String url, String payload) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload));
    }

    private org.springframework.test.web.servlet.ResultActions postJsonFromIp(String url, String payload, String ip) throws Exception {
        return mockMvc.perform(post(url)
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload));
    }

    private Map<String, String> cookies(MvcResult result) {
        Map<String, String> map = new HashMap<>();
        for (String header : result.getResponse().getHeaders(HttpHeaders.SET_COOKIE)) {
            for (HttpCookie httpCookie : HttpCookie.parse(header)) {
                map.put(httpCookie.getName(), httpCookie.getValue());
            }
        }
        return map;
    }

    private String extractCode(String body) {
        for (String part : body.split("\\s+")) {
            if (part.matches("\\d{6}")) {
                return part.trim();
            }
        }
        return "";
    }
}
