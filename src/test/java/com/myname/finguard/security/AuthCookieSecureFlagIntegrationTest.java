package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
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
        "app.security.jwt.cookie-secure=true",
        "app.security.otp.enabled=false",
        "app.security.tokens.reset-cooldown-seconds=0"
})
class AuthCookieSecureFlagIntegrationTest {

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
    private PendingRegistrationRepository pendingRegistrationRepository;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @AfterEach
    void cleanup() {
        userSessionRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        userRepository.deleteAll();
        tokenBlacklistService.clearAll();
        mailService.clearOutbox();
    }

    @Test
    void authCookiesContainSecureFlagWhenEnabled() throws Exception {
        Csrf csrf = fetchCsrf();
        String email = "secure-" + UUID.randomUUID() + "@example.com";
        mailService.clearOutbox();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"StrongPass1!","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isCreated());

        String code = extractLatestCode();
        MvcResult verified = mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code))
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(findSetCookie(verified, "FG_AUTH")).contains("Secure");
        assertThat(findSetCookie(verified, "FG_REFRESH")).contains("Secure");
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

