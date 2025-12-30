package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.auth.repository.PasswordResetSessionRepository;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
import com.myname.finguard.auth.repository.UserTokenRepository;
import com.myname.finguard.common.service.MailService;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        "app.security.csrf.enabled=true",
        "app.security.jwt.cookie-secure=false",
        "app.security.otp.enabled=false",
        "app.security.tokens.reset-cooldown-seconds=0"
})
class AuthCookieSecurityIntegrationTest {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\b(\\d{6})\\b");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private PasswordResetSessionRepository passwordResetSessionRepository;
    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private RateLimiterService rateLimiterService;

    @AfterEach
    void cleanup() {
        passwordResetSessionRepository.deleteAll();
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        userRepository.deleteAll();
        tokenBlacklistService.clearAll();
        rateLimiterService.reset();
        mailService.clearOutbox();
    }

    @Test
    void verifyAndRefreshSetAuthCookiesWithExpectedFlags() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "cookie-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);

        MvcResult verified = verify(email, extractLatestCode(), csrf);
        assertAuthCookieFlags(verified, "FG_AUTH", false);
        assertAuthCookieFlags(verified, "FG_REFRESH", false);

        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(refreshCookie).isNotNull();
        String refresh = refreshCookie.getValue();
        assertThat(refresh).isNotBlank();

        // Refresh ignores Authorization and only reads FG_REFRESH cookie.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        // Refresh ignores wrong cookie name.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .cookie(new Cookie("FG_AUTH", refresh)))
                .andExpect(status().isUnauthorized());

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .cookie(new Cookie("FG_REFRESH", refresh)))
                .andExpect(status().isOk())
                .andReturn();

        assertAuthCookieFlags(refreshed, "FG_AUTH", false);
        assertAuthCookieFlags(refreshed, "FG_REFRESH", false);
    }

    @Test
    void registerDoesNotSetAuthCookies() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "no-cookies-" + UUID.randomUUID() + "@example.com";
        mailService.clearOutbox();

        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"StrongPass1!","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(findSetCookie(res, "FG_AUTH")).isBlank();
        assertThat(findSetCookie(res, "FG_REFRESH")).isBlank();
        assertThat(mailService.getOutbox()).isNotEmpty();
    }

    @Test
    void refreshRequiresCsrfHeader() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "csrf-refresh-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);
        MvcResult verified = verify(email, extractLatestCode(), csrf);

        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(refreshCookie).isNotNull();

        // CSRF cookie present but header missing -> forbidden.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .cookie(refreshCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void verifyRequiresCsrfHeader() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "csrf-verify-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);
        String code = extractLatestCode();

        // CSRF cookie present but header missing -> forbidden.
        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code))
                        .cookie(csrf.cookie()))
                .andExpect(status().isForbidden());

        // With header -> ok.
        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isOk());
    }

    @Test
    void cookieAuthWorksOnProtectedEndpoint() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "cookie-auth-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);
        MvcResult verified = verify(email, extractLatestCode(), csrf);

        Cookie accessCookie = verified.getResponse().getCookie("FG_AUTH");
        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.getValue()).isNotBlank();

        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        // Wrong cookie name should not authenticate.
        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(new Cookie("FG_REFRESH", accessCookie.getValue())))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutBlacklistsOldAccessCookie() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "logout-blacklist-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);
        MvcResult verified = verify(email, extractLatestCode(), csrf);

        Cookie accessCookie = verified.getResponse().getCookie("FG_AUTH");
        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(accessCookie).isNotNull();
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .cookie(accessCookie)
                        .cookie(refreshCookie))
                .andExpect(status().isOk());

        // Old cookie value should no longer authenticate.
        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(new Cookie("FG_AUTH", accessCookie.getValue())))
                .andExpect(status().isForbidden());
    }

    @Test
    void passwordResetInvalidatesExistingCookieTokens() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "reset-cookie-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);
        MvcResult verified = verify(email, extractLatestCode(), csrf);

        Cookie accessCookie = verified.getResponse().getCookie("FG_AUTH");
        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(accessCookie).isNotNull();
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(post("/api/auth/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isOk());

        String resetCode = extractLatestCode();
        MvcResult confirm = mockMvc.perform(post("/api/auth/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, resetCode))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isOk())
                .andReturn();
        String resetSessionToken = objectMapper
                .readTree(confirm.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("resetSessionToken").asText();
        assertThat(resetSessionToken).isNotBlank();

        mockMvc.perform(post("/api/auth/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resetSessionToken":"%s","password":"NewStrong1!"}
                                """.formatted(resetSessionToken))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isOk());

        // Old cookie tokens should no longer authenticate / refresh.
        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(new Cookie("FG_AUTH", accessCookie.getValue())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .cookie(new Cookie("FG_REFRESH", refreshCookie.getValue())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutExpiresAuthCookies() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "logout-" + UUID.randomUUID() + "@example.com";
        register(email, "StrongPass1!", csrf);
        MvcResult verified = verify(email, extractLatestCode(), csrf);

        Cookie accessCookie = verified.getResponse().getCookie("FG_AUTH");
        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(accessCookie).isNotNull();
        assertThat(refreshCookie).isNotNull();

        MvcResult logout = mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .cookie(accessCookie)
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andReturn();

        String accessSetCookie = findSetCookie(logout, "FG_AUTH");
        assertThat(accessSetCookie).contains("Max-Age=0");
        assertThat(accessSetCookie).contains("HttpOnly");
        assertThat(accessSetCookie).contains("Path=/");
        assertThat(accessSetCookie).contains("SameSite=Strict");
        assertThat(accessSetCookie).doesNotContain("Secure");

        String refreshSetCookie = findSetCookie(logout, "FG_REFRESH");
        assertThat(refreshSetCookie).contains("Max-Age=0");
        assertThat(refreshSetCookie).contains("HttpOnly");
        assertThat(refreshSetCookie).contains("Path=/");
        assertThat(refreshSetCookie).contains("SameSite=Strict");
        assertThat(refreshSetCookie).doesNotContain("Secure");
    }

    @Test
    void csrfCookieIsNotHttpOnly() throws Exception {
        MvcResult csrfRes = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String xsrfSetCookie = findSetCookie(csrfRes, "XSRF-TOKEN");
        assertThat(xsrfSetCookie).isNotBlank();
        assertThat(xsrfSetCookie).doesNotContain("HttpOnly");
    }

    private void register(String email, String password, Csrf csrf) throws Exception {
        mailService.clearOutbox();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email, password))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isCreated());
    }

    private MvcResult verify(String email, String code, Csrf csrf) throws Exception {
        return mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Csrf fetchCsrf() throws Exception {
        MvcResult csrfRes = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(csrfRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = body.get("token").asText();
        Cookie cookie = csrfRes.getResponse().getCookie("XSRF-TOKEN");
        assertThat(token).isNotBlank();
        assertThat(cookie).isNotNull();
        return new Csrf(token, cookie);
    }

    private String extractLatestCode() {
        List<MailService.MailMessage> outbox = mailService.getOutbox();
        assertThat(outbox).isNotEmpty();
        MailService.MailMessage last = outbox.get(outbox.size() - 1);
        Matcher matcher = CODE_PATTERN.matcher(last.body());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private void assertAuthCookieFlags(MvcResult res, String name, boolean secureExpected) {
        String header = findSetCookie(res, name);
        assertThat(header).isNotBlank();
        assertThat(header).contains("HttpOnly");
        assertThat(header).contains("Path=/");
        assertThat(header).contains("SameSite=Strict");
        assertThat(header).contains("Max-Age=");
        assertThat(header).doesNotContain("Max-Age=0");
        if (secureExpected) {
            assertThat(header).contains("Secure");
        } else {
            assertThat(header).doesNotContain("Secure");
        }
    }

    private String findSetCookie(MvcResult result, String cookieName) {
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        return cookies.stream()
                .filter(h -> h != null && h.startsWith(cookieName + "="))
                .findFirst()
                .orElse("");
    }

    private record Csrf(String token, Cookie cookie) {
    }
}
