package com.yourname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourname.finguard.auth.dto.ForgotPasswordRequest;
import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.model.UserToken;
import com.yourname.finguard.auth.model.UserTokenType;
import com.yourname.finguard.auth.repository.UserRepository;
import com.yourname.finguard.auth.repository.UserSessionRepository;
import com.yourname.finguard.auth.repository.UserTokenRepository;
import com.yourname.finguard.common.service.MailService;
import com.yourname.finguard.security.LoginAttemptService;
import com.yourname.finguard.security.RateLimiterService;
import com.yourname.finguard.security.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
        "app.security.lockout.max-attempts=2",
        "app.security.lockout.lock-minutes=60",
        "app.security.sessions.max-per-user=2",
        "app.security.pwned-check.enabled=false",
        "app.security.jwt.require-env-secret=false",
        "app.security.cors.allowed-origins=http://example.com"
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
    private UserSessionRepository userSessionRepository;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private MailService mailService;
    @Value("${app.security.jwt.secret}")
    private String jwtSecret;
    @Value("${app.security.jwt.issuer}")
    private String issuer;
    @Value("${app.security.jwt.audience}")
    private String audience;

    @AfterEach
    void cleanup() {
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        userRepository.deleteAll();
        tokenBlacklistService.clearAll();
        rateLimiterService.reset();
        loginAttemptService.reset();
        mailService.clearOutbox();
    }

    @Test
    void registrationNormalizesAndValidates() throws Exception {
        String emailRaw = "User+tag@Example.Com";
        String password = "StrongPass1!";
        String payload = """
                {"email":"%s","password":"%s","fullName":"  Test User  ","baseCurrency":"usd"}
                """.formatted(emailRaw, password);

        MvcResult register = postJson("/api/auth/register", payload)
                .andExpect(status().isCreated())
                .andReturn();
        String access = accessToken(register);

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
        registerUser("dupe@example.com", "StrongPass1!");
        MvcResult res = postJson("/api/auth/register", """
                {"email":"DUPE@EXAMPLE.COM","password":"StrongPass1!","fullName":"User2","baseCurrency":"USD"}
                """)
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100002");
    }

    @Test
    void lockoutBlocksAfterMaxAttempts() throws Exception {
        String email = "lock@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

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
    void refreshRotationRevokesOldTokens() throws Exception {
        String email = "refresh@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerUser(email, "StrongPass1!");
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
        MvcResult reg = registerUser(email, "StrongPass1!");
        String access = accessToken(reg);
        Map<String, String> cookies = cookies(reg);

        postLogout(cookies).andExpect(status().isOk());

        // access token is blacklisted -> cannot fetch profile
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void verificationFlowMarksUserVerified() throws Exception {
        String email = "verify@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

        postJson("/api/auth/verify/request", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());

        UserToken token = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.VERIFY)
                .findFirst()
                .orElseThrow();

        postJson("/api/auth/verify", """
                {"token":"%s"}
                """.formatted(token.getToken()))
                .andExpect(status().isOk());

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.isEmailVerified()).isTrue();
        UserToken used = userTokenRepository.findById(token.getId()).orElseThrow();
        assertThat(used.getUsedAt()).isNotNull();

        // Reuse token is a no-op and stays verified
        postJson("/api/auth/verify", """
                {"token":"%s"}
                """.formatted(token.getToken()))
                .andExpect(status().isOk());
        assertThat(userRepository.findByEmail(email).orElseThrow().isEmailVerified()).isTrue();
    }

    @Test
    void resetFlowChangesPasswordAndInvalidatesToken() throws Exception {
        String email = "reset@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());

        UserToken token = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.RESET)
                .findFirst()
                .orElseThrow();

        postJson("/api/auth/reset", """
                {"token":"%s","password":"NewStrong1!"}
                """.formatted(token.getToken()))
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
                {"token":"%s","password":"AnotherStrong1!"}
                """.formatted(token.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotSendsResetEmailTemplateWithDevCode() throws Exception {
        String email = "mail@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
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
        assertThat(message.subject()).contains("Сброс пароля");
        assertThat(message.body()).contains(token.getToken());
        assertThat(message.body()).contains("123456");
        assertThat(message.body()).contains("/app/reset.html");
    }

    @Test
    void resetRejectsWeakPassword() throws Exception {
        String email = "reset-weak@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest(email)))
                .andExpect(status().isOk());
        UserToken token = userTokenRepository.findAll().stream()
                .filter(t -> t.getType() == UserTokenType.RESET)
                .findFirst()
                .orElseThrow();

        MvcResult res = postJson("/api/auth/reset", """
                {"token":"%s","password":"password1"}
                """.formatted(token.getToken()))
                .andExpect(status().isBadRequest())
                .andReturn();
        JsonNode error = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("400003");
    }

    @Test
    void multipleSessionsPruneOldRefreshTokens() throws Exception {
        String email = "sessions@" + UUID.randomUUID() + ".com";
        MvcResult login1 = registerUser(email, "StrongPass1!");
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
        MvcResult reg = registerUser(email, "StrongPass1!");
        String access = cookies(reg).get("FG_AUTH");
        postJsonWithCookie("/api/auth/refresh", "{}", access)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRemovesSessionsAndBlocksRefresh() throws Exception {
        String email = "logout-session@" + UUID.randomUUID() + ".com";
        MvcResult reg = registerUser(email, "StrongPass1!");
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
        MvcResult reg = registerUser(email, "StrongPass1!");
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
        token.setToken(UUID.randomUUID().toString());
        token.setType(UserTokenType.VERIFY);
        token.setExpiresAt(Instant.now().minusSeconds(60));
        userTokenRepository.save(token);

        postJson("/api/auth/verify", """
                {"token":"%s"}
                """.formatted(token.getToken()))
                .andExpect(status().isOk());

        User fresh = userRepository.findByEmail(email).orElseThrow();
        assertThat(fresh.isEmailVerified()).isFalse();
    }

    @Test
    void expiredResetTokenRejected() throws Exception {
        String email = "expired-reset@" + UUID.randomUUID() + ".com";
        registerUser(email, "StrongPass1!");

        User user = userRepository.findByEmail(email).orElseThrow();
        UserToken token = new UserToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setType(UserTokenType.RESET);
        token.setExpiresAt(Instant.now().minusSeconds(60));
        userTokenRepository.save(token);

        postJson("/api/auth/reset", """
                {"token":"%s","password":"AnotherStrong1!"}
                """.formatted(token.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPasswordUnknownEmailStillReturnsOk() throws Exception {
        postJson("/api/auth/forgot", objectMapper.writeValueAsString(new ForgotPasswordRequest("nouser@" + UUID.randomUUID() + ".com")))
                .andExpect(status().isOk());
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

    private MvcResult registerUser(String email, String password) throws Exception {
        String payload = """
                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                """.formatted(email, password);
        return postJson("/api/auth/register", payload).andExpect(status().isCreated()).andReturn();
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

    private ResultActions postJsonWithCookie(String url, String payload, String cookieValue) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .cookie(new Cookie("FG_REFRESH", cookieValue)));
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
