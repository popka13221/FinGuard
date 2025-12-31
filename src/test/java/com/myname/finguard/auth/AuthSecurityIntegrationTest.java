package com.myname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.dto.ForgotPasswordRequest;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.model.PendingRegistration;
import com.myname.finguard.auth.model.PasswordResetSession;
import com.myname.finguard.auth.model.UserToken;
import com.myname.finguard.auth.model.UserTokenType;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.PasswordResetSessionRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
import com.myname.finguard.auth.repository.UserTokenRepository;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.security.LoginAttemptService;
import com.myname.finguard.security.RateLimiterService;
import com.myname.finguard.security.JwtTokenProvider;
import com.myname.finguard.security.TokenBlacklistService;
import com.myname.finguard.auth.service.UserTokenService;
import jakarta.servlet.http.Cookie;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.rate-limit.auth.limit=5",
        "app.security.rate-limit.auth.window-ms=5000",
        "app.security.rate-limit.forgot.limit=5",
        "app.security.rate-limit.forgot.window-ms=60000",
        "app.security.tokens.reset-cooldown-seconds=0",
        "app.security.rate-limit.reset-confirm.limit=3",
        "app.security.rate-limit.reset-confirm.window-ms=60000",
        "app.security.rate-limit.reset.limit=3",
        "app.security.rate-limit.reset.window-ms=60000",
        "app.security.rate-limit.login-email.limit=3",
        "app.security.rate-limit.login-email.window-ms=60000",
        "app.security.trust-proxy-headers=true",
        "app.security.lockout.max-attempts=2",
        "app.security.lockout.lock-minutes=60",
        "app.security.sessions.max-per-user=2",
        "app.security.pwned-check.enabled=false",
        "app.security.jwt.require-env-secret=false",
        "app.security.cors.allowed-origins=http://example.com",
        "app.security.tokens.reset-session-ttl-minutes=15"
})
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;
    @Autowired
    private PasswordResetSessionRepository passwordResetSessionRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private MailService mailService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private com.myname.finguard.auth.service.PasswordResetSessionService passwordResetSessionService;
    @Autowired
    private UserTokenService userTokenService;
    @Value("${app.security.jwt.secret}")
    private String jwtSecret;
    @Value("${app.security.jwt.issuer}")
    private String issuer;
    @Value("${app.security.jwt.audience}")
    private String audience;

    @AfterEach
    void cleanup() {
        passwordResetSessionRepository.deleteAll();
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        userRepository.deleteAll();
        tokenBlacklistService.clearAll();
        rateLimiterService.reset();
        loginAttemptService.reset();
        mailService.clearOutbox();
    }

    @BeforeEach
    void cleanRateLimiter() {
        // H2 in-memory DB is shared across Spring test contexts; reset buckets to avoid cross-test interference.
        rateLimiterService.reset();
    }

    @Test
    void registrationNormalizesAndValidates() throws Exception {
        String emailRaw = "User+tag@Example.Com";
        String password = "StrongPass1!";
        String payload = """
                {"email":"%s","password":"%s","fullName":"  Test User  ","baseCurrency":"usd"}
                """.formatted(emailRaw, password);

        postJson("/api/auth/register", payload)
                .andExpect(status().isCreated())
                .andReturn();
        String email = "user+tag@example.com";
        String verifyCode = extractCode(latestMail().body());
        MvcResult verified = postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, verifyCode))
                .andExpect(status().isOk())
                .andReturn();
        String access = accessToken(verified);

        // profile reflects normalized email/currency
        JsonNode profile = getProfile(access);
        assertThat(profile.get("email").asText()).isEqualTo("user+tag@example.com");
        assertThat(profile.get("baseCurrency").asText()).isEqualTo("USD");

        // persisted user normalized
        Optional<User> user = userRepository.findByEmail("user+tag@example.com");
        assertThat(user).isPresent();
        assertThat(user.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    void registrationRejectsUnsupportedCurrency() throws Exception {
        String payload = """
                {"email":"badcurrency@example.com","password":"StrongPass1!","fullName":"User","baseCurrency":"ZZZZ"}
                """;
        MvcResult res = postJson("/api/auth/register", payload)
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("400000");
    }

    @Test
    void registrationRejectsWeakPassword() throws Exception {
        String payload = """
                {"email":"weak@example.com","password":"Password1!","fullName":"User","baseCurrency":"USD"}
                """;
        MvcResult res = postJson("/api/auth/register", payload)
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100003");
    }

    @Test
    void registrationRejectsInvalidPatternPassword() throws Exception {
        String payload = """
                {"email":"invalidpattern@example.com","password":"short","fullName":"User","baseCurrency":"USD"}
                """;
        MvcResult res = postJson("/api/auth/register", payload)
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("400003");
    }

    @Test
    void registrationRejectsDuplicateEmailCaseInsensitive() throws Exception {
        registerVerifiedUser("dupe@example.com", "StrongPass1!");
        MvcResult res = postJson("/api/auth/register", """
                {"email":"DUPE@EXAMPLE.COM","password":"StrongPass1!","fullName":"User2","baseCurrency":"USD"}
                """)
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100002");
    }

    @Test
    void reRegisterUpdatesPendingPasswordHash() throws Exception {
        String email = "pending-update@" + UUID.randomUUID() + ".com";

        postJson("/api/auth/register", """
                {"email":"%s","password":"StrongPass1!","fullName":"User","baseCurrency":"USD"}
                """.formatted(email))
                .andExpect(status().isCreated());

        postJson("/api/auth/register", """
                {"email":"%s","password":"OtherStrong1!","fullName":"User","baseCurrency":"USD"}
                """.formatted(email))
                .andExpect(status().isCreated());

        // Old password should be treated as invalid credentials (pending password was updated).
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isUnauthorized());

        // New password matches pending registration -> "email not verified" (403) even though user does not exist yet.
        MvcResult blocked = postJson("/api/auth/login", """
                {"email":"%s","password":"OtherStrong1!"}
                """.formatted(email))
                .andExpect(status().isForbidden())
                .andReturn();
        JsonNode error = objectMapper.readTree(blocked.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100006");
    }

    @Test
    void expiredPendingRegistrationCannotBeVerifiedOrUsedForNotVerifiedLogin() throws Exception {
        String email = "pending-expired@" + UUID.randomUUID() + ".com";
        postJson("/api/auth/register", """
                {"email":"%s","password":"StrongPass1!","fullName":"User","baseCurrency":"USD"}
                """.formatted(email))
                .andExpect(status().isCreated());

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(email).orElseThrow();
        pending.setVerifyExpiresAt(Instant.now().minusSeconds(5));
        pendingRegistrationRepository.save(pending);

        postJson("/api/auth/verify", """
                {"email":"%s","token":"654321"}
                """.formatted(email))
                .andExpect(status().isBadRequest());

        assertThat(pendingRegistrationRepository.findByEmail(email)).isEmpty();
        assertThat(userRepository.findByEmail(email)).isEmpty();

        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRequiresVerifiedEmail() throws Exception {
        String email = "unverified@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

        MvcResult blocked = postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isForbidden())
                .andReturn();
        JsonNode error = objectMapper.readTree(blocked.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100006");

        String verifyCode = extractCode(latestMail().body());
        postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, verifyCode))
                .andExpect(status().isOk());
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isOk());
    }

    @Test
    void lockoutBlocksAfterMaxAttempts() throws Exception {
        String email = "lock@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");

        String badPayload = """
                {"email":"%s","password":"WrongPass1!"}
                """.formatted(email);

        postJson("/api/auth/login", badPayload).andExpect(status().isUnauthorized());
        postJson("/api/auth/login", badPayload).andExpect(status().isUnauthorized());

        MvcResult locked = postJson("/api/auth/login", badPayload)
                .andExpect(status().isTooManyRequests())
                .andReturn();
        JsonNode error = objectMapper.readTree(locked.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100004");

        // even correct password is blocked while locked
        String goodPayload = """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email);
        postJson("/api/auth/login", goodPayload).andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimitBlocksExcessiveRequests() throws Exception {
        // Use refresh endpoint to avoid lockout side effects
        postJson("/api/auth/refresh", "{}").andExpect(status().isUnauthorized());
        postJson("/api/auth/refresh", "{}").andExpect(status().isUnauthorized());
        postJson("/api/auth/refresh", "{}").andExpect(status().isUnauthorized());
        postJson("/api/auth/refresh", "{}").andExpect(status().isUnauthorized());
        postJson("/api/auth/refresh", "{}").andExpect(status().isUnauthorized());
        MvcResult limited = postJson("/api/auth/refresh", "{}")
                .andExpect(status().isTooManyRequests())
                .andReturn();
        JsonNode error = objectMapper.readTree(limited.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("429001");
    }

    @Test
    void rateLimitUsesForwardedForWhenTrusted() throws Exception {
        for (int i = 0; i < 5; i++) {
            postJsonWithForwarded("/api/auth/refresh", "{}", "10.0.0.1")
                    .andExpect(status().isUnauthorized());
        }
        postJsonWithForwarded("/api/auth/refresh", "{}", "10.0.0.1")
                .andExpect(status().isTooManyRequests());

        // Different forwarded IP should have its own bucket
        postJsonWithForwarded("/api/auth/refresh", "{}", "10.0.0.2")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotationRevokesOldTokens() throws Exception {
        String email = "refresh@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerVerifiedUser(email, "StrongPass1!");
        Map<String, String> cookies = cookies(reg);

        MvcResult refreshed = postJsonWithCookie("/api/auth/refresh", "{}", cookies.get("FG_REFRESH"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, String> refreshedCookies = cookies(refreshed);

        // old refresh should now be invalid
        postJsonWithCookie("/api/auth/refresh", "{}", cookies.get("FG_REFRESH"))
                .andExpect(status().isUnauthorized());

        // new refresh works once
        postJsonWithCookie("/api/auth/refresh", "{}", refreshedCookies.get("FG_REFRESH"))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRevokesAccessAndRefresh() throws Exception {
        String email = "logout@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerVerifiedUser(email, "StrongPass1!");
        String access = accessToken(reg);
        Map<String, String> cookies = cookies(reg);

        postLogout(cookies).andExpect(status().isOk());

        // access token is blacklisted -> cannot fetch profile
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void verificationFlowCreatesUserAndClearsPending() throws Exception {
        String email = "verify@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

        assertThat(userRepository.findByEmail(email)).isEmpty();
        assertThat(pendingRegistrationRepository.findByEmail(email)).isPresent();

        String rawToken = extractCode(latestMail().body());
        MvcResult verified = postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, rawToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(verified.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(body.get("token").asText()).isNotBlank();

        User created = userRepository.findByEmail(email).orElseThrow();
        assertThat(created.isEmailVerified()).isTrue();
        assertThat(pendingRegistrationRepository.findByEmail(email)).isEmpty();

        // Reuse token should fail (pending registration already consumed)
        postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, rawToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetFlowChangesPasswordAndInvalidatesToken() throws Exception {
        String email = "reset@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        assertThat(userTokenService.getResetTtl().toMinutes()).isGreaterThan(0);

        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());

        UserToken token = latestResetToken();
        assertThat(token.getExpiresAt()).isAfter(token.getCreatedAt());
        assertThat(Duration.between(token.getCreatedAt(), token.getExpiresAt()).getSeconds()).isGreaterThan(0);
        List<com.myname.finguard.auth.model.UserSession> sessionsBefore = userSessionRepository.findByUserId(token.getUser().getId());
        assertThat(sessionsBefore).isNotEmpty();
        String resetCode = extractCode(latestMail().body());
        String resetSessionToken = confirmResetSession(email, resetCode);
        String resetJti = jwtTokenProvider.getJti(resetSessionToken);
        String resetTokenHash = passwordResetSessionService.hashToken(resetJti);
        PasswordResetSession resetSession = passwordResetSessionRepository.findByTokenHash(resetTokenHash).orElseThrow();
        assertThat(resetSession.getTokenHash()).isEqualTo(resetTokenHash);
        assertThat(resetSession.getTokenHash()).isNotEqualTo(resetJti);
        assertThat(resetSession.getExpiresAt()).isNotNull();
        assertThat(resetSession.getExpiresAt()).isAfter(Instant.now().minusSeconds(1));
        assertThat(passwordResetSessionService.matchesContext(resetSession, "127.0.0.1", null)).isTrue();
        assertThat(passwordResetSessionService.findActive(resetJti)).isPresent();

        postJson("/api/auth/reset", """
                {"resetSessionToken":"%s","password":"NewStrong1!"}
                """.formatted(resetSessionToken))
                .andExpect(status().isOk());

        // old password rejected, new works
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isUnauthorized());

        postJson("/api/auth/login", """
                {"email":"%s","password":"NewStrong1!"}
                """.formatted(email)).andExpect(status().isOk());

        // reuse reset token blocked
        postJson("/api/auth/reset", """
                {"resetSessionToken":"%s","password":"AnotherStrong1!"}
                """.formatted(resetSessionToken))
                .andExpect(status().isBadRequest());

        List<com.myname.finguard.auth.model.UserSession> sessionsAfter = userSessionRepository.findByUserId(token.getUser().getId());
        assertThat(sessionsAfter).hasSize(1);
        assertThat(sessionsBefore.stream().map(com.myname.finguard.auth.model.UserSession::getJti).toList())
                .doesNotContain(sessionsAfter.get(0).getJti());
        sessionsBefore.forEach(session -> assertThat(tokenBlacklistService.isRevoked(session.getJti())).isTrue());
    }

    @Test
    void forgotSendsResetEmailTemplateWithDevCode() throws Exception {
        String email = "mail@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        mailService.clearOutbox();

        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());

        UserToken token = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.RESET)
                .findFirst()
                .orElseThrow();

        assertThat(mailService.getOutbox()).isNotEmpty();
        MailService.MailMessage message = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        assertThat(message.to()).isEqualTo(email);
        assertThat(message.subject()).containsIgnoringCase("password reset");
        assertThat(message.body()).contains("654321");
        assertThat(message.body()).contains("/app/reset.html");
    }

    @Test
    void forgotWithDevCodeCanBeRequestedMultipleTimes() throws Exception {
        String email = "multi@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        mailService.clearOutbox();

        for (int i = 0; i < 3; i++) {
            postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                    .andExpect(status().isOk());
        }

        var resetTokens = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.RESET)
                .toList();
        assertThat(resetTokens).hasSize(3);
        assertThat(resetTokens.stream().filter(t -> t.getUsedAt() == null)).hasSize(1);
        UserToken token = resetTokens.stream()
                .filter(t -> t.getUsedAt() == null)
                .findFirst()
                .orElseThrow();
        assertThat(token.getTokenHash()).isEqualTo(hashToken("654321"));

        assertThat(mailService.getOutbox()).hasSize(3);
        MailService.MailMessage last = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        assertThat(last.body()).contains("654321");
    }

    @Test
    void verifyCreatesUserFromPendingRegistration() throws Exception {
        String email = "verify-dev@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

        var pending = pendingRegistrationRepository.findByEmail(email);
        assertThat(pending).isPresent();
        assertThat(pending.get().getVerifyTokenHash()).isEqualTo(hashToken("654321"));

        MailService.MailMessage last = latestMail();
        assertThat(last).isNotNull();
        assertThat(last.subject()).containsIgnoringCase("verification");
        assertThat(last.body()).contains("654321");
        assertThat(last.body()).contains("/app/verify.html");

        postJson("/api/auth/verify", """
                {"email":"%s","token":"654321"}
                """.formatted(email))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(email)).isPresent();
        assertThat(userRepository.findByEmail(email).orElseThrow().isEmailVerified()).isTrue();
        assertThat(pendingRegistrationRepository.findByEmail(email)).isEmpty();

        var verifyTokens = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.VERIFY)
                .toList();
        assertThat(verifyTokens).isEmpty();
    }

    @Test
    void verifyRequestReissuesDevCodeForPendingRegistration() throws Exception {
        String email = "verify-repeat@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        mailService.clearOutbox();

        postJson("/api/auth/verify/request", """
                {"email":"%s"}
                """.formatted(email))
                .andExpect(status().isOk());

        var pending = pendingRegistrationRepository.findByEmail(email);
        assertThat(pending).isPresent();
        assertThat(pending.get().getVerifyTokenHash()).isEqualTo(hashToken("654321"));

        MailService.MailMessage last = latestMail();
        assertThat(last).isNotNull();
        assertThat(last.body()).contains("654321");

        var verifyTokens = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.VERIFY)
                .toList();
        assertThat(verifyTokens).isEmpty();
    }

    @Test
    void resetRejectsWeakPassword() throws Exception {
        String email = "reset-weak@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());
        UserToken token = latestResetToken();
        String resetSessionToken = confirmResetSession(email, latestResetCode());

        MvcResult res = postJson("/api/auth/reset", """
                {"resetSessionToken":"%s","password":"password1"}
                """.formatted(resetSessionToken))
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("400003");
    }

    @Test
    void resetRejectsInvalidToken() throws Exception {
        String email = "invalid-token@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        MvcResult res = postJson("/api/auth/reset", """
                {"resetSessionToken":"invalid-token-123","password":"NewStrong1!"}
                """)
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100005");
        assertThat(error.get("message").asText()).containsIgnoringCase("invalid");
    }

    @Test
    void resetAllowsSoftContextMismatchWhenUserAgentMatches() throws Exception {
        String email = "soft-context@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());
        UserToken token = latestResetToken();

        MvcResult confirm = postJsonFromIpAndUa("/api/auth/reset/confirm", """
                {"email":"%s","token":"%s"}
                """.formatted(email, latestResetCode()), "10.0.0.1", "TestUA/1.0")
                .andExpect(status().isOk())
                .andReturn();
        String resetSessionToken = objectMapper.readTree(confirm.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("resetSessionToken").asText();

        postJsonFromIpAndUa("/api/auth/reset", """
                {"resetSessionToken":"%s","password":"SoftChange1!"}
                """.formatted(resetSessionToken), "10.0.0.2", "TestUA/1.0")
                .andExpect(status().isOk());
    }

    @Test
    void resetRejectsWhenIpAndUserAgentChange() throws Exception {
        String email = "context-reject@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());
        UserToken token = latestResetToken();

        MvcResult confirm = postJsonFromIpAndUa("/api/auth/reset/confirm", """
                {"email":"%s","token":"%s"}
                """.formatted(email, latestResetCode()), "10.0.0.1", "TestUA/1.0")
                .andExpect(status().isOk())
                .andReturn();
        String resetSessionToken = objectMapper.readTree(confirm.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("resetSessionToken").asText();

        MvcResult res = postJsonFromIpAndUa("/api/auth/reset", """
                {"resetSessionToken":"%s","password":"RejectChange1!"}
                """.formatted(resetSessionToken), "10.0.0.5", "OtherUA/2.0")
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100005");

        // Password remains unchanged after rejection
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isOk());
    }

    @Test
    void validateResetTokenEndpointWorks() throws Exception {
        String email = "check-token@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());

        MvcResult okRes = postJson("/api/auth/reset/confirm", """
                {"email":"%s","token":"%s"}
                """.formatted(email, latestResetCode()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode okBody = objectMapper.readTree(okRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(okBody.get("resetSessionToken").asText()).isNotBlank();

        MvcResult res = postJson("/api/auth/reset/confirm", """
                {"email":"%s","token":"invalid-token"}
                """.formatted(email))
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100005");
    }

    @Test
    void resetConfirmRateLimitRequiresNewCode() throws Exception {
        String email = "confirm-rl@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");

        for (int i = 0; i < 3; i++) {
            postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                    .andExpect(status().isOk());
            postJsonFromIp("/api/auth/reset/confirm", """
                    {"email":"%s","token":"%s"}
                    """.formatted(email, latestResetCode()), "127.0.0." + (10 + i))
                    .andExpect(status().isOk());
        }

        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());
        MvcResult limited = postJsonFromIp("/api/auth/reset/confirm", """
                {"email":"%s","token":"%s"}
                """.formatted(email, latestResetCode()), "127.0.0.50")
                .andExpect(status().isTooManyRequests())
                .andReturn();
        JsonNode error = objectMapper.readTree(limited.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100005");
        assertThat(userTokenService.findAnyForEmail(email, latestResetCode(), UserTokenType.RESET)
                .filter(t -> t.getUsedAt() == null)
                .filter(t -> t.getExpiresAt() != null && t.getExpiresAt().isAfter(Instant.now()))).isEmpty();
    }

    @Test
    void forgotRateLimitBlocksTooManyRequests() throws Exception {
        String email = "rl-forgot@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");

        for (int i = 0; i < 5; i++) {
            postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                    .andExpect(status().isOk());
        }
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void multipleSessionsPruneOldRefreshTokens() throws Exception {
        String email = "sessions@" + UUID.randomUUID() + ".com";
        MvcResult login1 = registerVerifiedUser(email, "StrongPass1!");
        Map<String, String> c1 = cookies(login1);

        // Second login (new session)
        MvcResult login2 = login(email, "StrongPass1!");
        Map<String, String> c2 = cookies(login2);

        // Third login triggers pruning (max 2 sessions), so first refresh becomes inactive
        MvcResult login3 = login(email, "StrongPass1!");
        Map<String, String> c3 = cookies(login3);

        postJsonWithCookie("/api/auth/refresh", "{}", c1.get("FG_REFRESH"))
                .andExpect(status().isUnauthorized());
        postJsonWithCookie("/api/auth/refresh", "{}", c2.get("FG_REFRESH"))
                .andExpect(status().isOk());
        postJsonWithCookie("/api/auth/refresh", "{}", c3.get("FG_REFRESH"))
                .andExpect(status().isOk());
    }

    @Test
    void corsAllowsConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .isEqualTo("http://example.com"))
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                        .isEqualTo("true"));
    }

    @Test
    void corsBlocksUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://unknown.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void profileWithoutAuthRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshEndpointRejectsAccessTokenInCookie() throws Exception {
        String email = "acc-refresh@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerVerifiedUser(email, "StrongPass1!");
        String access = cookies(reg).get("FG_AUTH");
        postJsonWithCookie("/api/auth/refresh", "{}", access)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRemovesSessionsAndBlocksRefresh() throws Exception {
        String email = "logout-session@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerVerifiedUser(email, "StrongPass1!");
        Map<String, String> cookieMap = cookies(reg);
        assertThat(userSessionRepository.count()).isEqualTo(1);

        postLogout(cookieMap).andExpect(status().isOk());
        assertThat(userSessionRepository.count()).isZero();

        postJsonWithCookie("/api/auth/refresh", "{}", cookieMap.get("FG_REFRESH"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutBlacklistsAccessToken() throws Exception {
        String email = "logout-access@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerVerifiedUser(email, "StrongPass1!");
        Map<String, String> cookieMap = cookies(reg);
        String access = accessToken(reg);

        postLogout(cookieMap).andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidAudienceTokenRejected() throws Exception {
        String badToken = buildToken("other-aud", issuer, "access", 3600);
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + badToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidIssuerTokenRejected() throws Exception {
        String badToken = buildToken(audience, "other-issuer", "access", 3600);
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + badToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void expiredAccessTokenRejected() throws Exception {
        String expired = buildToken(audience, issuer, "access", -60);
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isForbidden());
    }

    @Test
    void expiredVerifyTokenDoesNothing() throws Exception {
        String email = "expired-verify@" + UUID.randomUUID() + ".com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setFullName("U");
        user.setBaseCurrency("USD");
        user = userRepository.save(user);

        UserToken token = new UserToken();
        token.setUser(user);
        String rawToken = UUID.randomUUID().toString();
        token.setTokenHash(hashToken(rawToken));
        token.setType(UserTokenType.VERIFY);
        token.setExpiresAt(Instant.now().minusSeconds(60));
        userTokenRepository.save(token);

        postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, rawToken))
                .andExpect(status().isBadRequest());

        User fresh = userRepository.findByEmail(email).orElseThrow();
        assertThat(fresh.isEmailVerified()).isFalse();
    }

    @Test
    void expiredResetTokenRejected() throws Exception {
        String email = "expired-reset@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");

        User user = userRepository.findByEmail(email).orElseThrow();
        UserToken token = new UserToken();
        token.setUser(user);
        String rawToken = UUID.randomUUID().toString();
        token.setTokenHash(hashToken(rawToken));
        token.setType(UserTokenType.RESET);
        token.setExpiresAt(Instant.now().minusSeconds(60));
        userTokenRepository.save(token);

        MvcResult res = postJson("/api/auth/reset/confirm", """
                {"email":"%s","token":"%s"}
                """.formatted(email, rawToken))
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("message").asText()).containsIgnoringCase("expired");
    }

    @Test
    void forgotPasswordUnknownEmailStillReturnsOk() throws Exception {
        MvcResult res = postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest("nouser@" + UUID.randomUUID() + ".com")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(body.get("message").asText()).containsIgnoringCase("email");
        assertThat(userTokenRepository.count()).isZero();
    }

    @Test
    void rateLimitAppliesToLoginEndpoint() throws Exception {
        String badPayload = "{}";
        for (int i = 0; i < 5; i++) {
            postJson("/api/auth/login", badPayload).andExpect(status().isBadRequest());
        }
        postJson("/api/auth/login", badPayload)
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimitAppliesToVerifyRequest() throws Exception {
        String payload = """
                {"email":"rate-limit-verify@example.com"}
                """;
        for (int i = 0; i < 5; i++) {
            postJson("/api/auth/verify/request", payload).andExpect(status().isOk());
        }
        postJson("/api/auth/verify/request", payload)
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void loginPerEmailRateLimitBlocksEvenWithSuccess() throws Exception {
        String email = "login-rl@" + UUID.randomUUID() + ".com";
        registerVerifiedUser(email, "StrongPass1!");

        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isOk());
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isOk());
        postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email)).andExpect(status().isOk());
        MvcResult limited = postJson("/api/auth/login", """
                {"email":"%s","password":"StrongPass1!"}
                """.formatted(email))
                .andExpect(status().isTooManyRequests())
                .andReturn();
        JsonNode error = objectMapper.readTree(limited.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("429001");
    }

    private UserToken latestResetToken() {
        return userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.RESET && t.getUsedAt() == null)
                .max(Comparator.comparing(UserToken::getCreatedAt))
                .orElseThrow();
    }

    private String confirmResetSession(String email, String rawToken) throws Exception {
        MvcResult res = postJson("/api/auth/reset/confirm", """
                {"email":"%s","token":"%s"}
                """.formatted(email, rawToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return node.get("resetSessionToken").asText();
    }

    private String latestResetCode() {
        MailService.MailMessage last = latestMail();
        return last == null ? "" : extractCode(last.body());
    }

    private MailService.MailMessage latestMail() {
        List<MailService.MailMessage> outbox = mailService.getOutbox();
        return outbox.isEmpty() ? null : outbox.get(outbox.size() - 1);
    }

    private String extractCode(String body) {
        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^[0-9a-fA-F-]{6,}$")) {
                return line;
            }
            String lower = line.toLowerCase();
            if (lower.contains("code:")) {
                int idx = lower.indexOf("code:");
                String tail = line.substring(Math.min(idx + 5, line.length())).trim();
                if (!tail.isBlank()) {
                    return tail.split("\\s+")[0].trim();
                }
            }
        }
        for (String line : lines) {
            for (String part : line.split("\\s+")) {
                if (part.matches("^[0-9a-fA-F-]{6,}$")) {
                    return part.trim();
                }
            }
        }
        return "";
    }

    private String hashToken(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MvcResult registerUser(String email, String password) throws Exception {
        String payload = """
                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                """.formatted(email, password);
        return postJson("/api/auth/register", payload).andExpect(status().isCreated()).andReturn();
    }

    private MvcResult registerVerifiedUser(String email, String password) throws Exception {
        registerUser(email, password);
        String verifyCode = extractCode(latestMail().body());
        return postJson("/api/auth/verify", """
                {"email":"%s","token":"%s"}
                """.formatted(email, verifyCode))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult login(String email, String password) throws Exception {
        String payload = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        return postJson("/api/auth/login", payload).andExpect(status().isOk()).andReturn();
    }

    private ResultActions postLogout(Map<String, String> cookies) throws Exception {
        return mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("FG_AUTH", cookies.get("FG_AUTH")))
                .cookie(new Cookie("FG_REFRESH", cookies.get("FG_REFRESH"))));
    }

    private JsonNode getProfile(String accessToken) throws Exception {
        MvcResult res = mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private ResultActions postJson(String url, String payload) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                ;
    }

    private ResultActions postJsonFromIp(String url, String payload, String ip) throws Exception {
        return mockMvc.perform(post(url)
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload));
    }

    private ResultActions postJsonFromIpAndUa(String url, String payload, String ip, String userAgent) throws Exception {
        return mockMvc.perform(post(url)
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .header(HttpHeaders.USER_AGENT, userAgent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload));
    }

    private ResultActions postJsonWithCookie(String url, String payload, String cookieValue) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .cookie(new Cookie("FG_REFRESH", cookieValue)));
    }

    private ResultActions postJsonWithForwarded(String url, String payload, String forwardedIp) throws Exception {
        return mockMvc.perform(post(url)
                        .header("X-Forwarded-For", forwardedIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload));
    }

    private String accessToken(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return node.get("token").asText();
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

    private String buildToken(String aud, String iss, String typ, long expOffsetSeconds) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        return Jwts.builder()
                .setSubject("bad@example.com")
                .setIssuer(iss)
                .setAudience(aud)
                .setId(UUID.randomUUID().toString())
                .claim("uid", 999)
                .claim("typ", typ)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(expOffsetSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
