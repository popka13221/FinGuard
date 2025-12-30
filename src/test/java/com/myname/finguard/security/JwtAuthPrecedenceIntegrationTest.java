package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.auth.repository.PasswordResetSessionRepository;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.auth.repository.UserSessionRepository;
import com.myname.finguard.auth.repository.UserTokenRepository;
import com.myname.finguard.common.service.MailService;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        "app.security.csrf.enabled=false",
        "app.security.otp.enabled=false",
        "app.security.tokens.reset-cooldown-seconds=0"
})
class JwtAuthPrecedenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;
    @Autowired
    private PasswordResetSessionRepository passwordResetSessionRepository;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void resetRateLimiter() {
        rateLimiterService.reset();
    }

    @AfterEach
    void cleanup() {
        passwordResetSessionRepository.deleteAll();
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        tokenBlacklistService.clearAll();
        rateLimiterService.reset();
        mailService.clearOutbox();
    }

    @Test
    void authorizationHeaderTakesPrecedenceOverValidCookie() throws Exception {
        MvcResult verified = registerAndVerify("prec1-" + UUID.randomUUID() + "@example.com");

        Cookie accessCookie = verified.getResponse().getCookie("FG_AUTH");
        assertThat(accessCookie).isNotNull();

        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        // Header is checked before cookies; invalid Authorization should not fall back to cookie auth.
        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(accessCookie)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizationHeaderOverridesCookieUserIdentity() throws Exception {
        String email1 = "prec2a-" + UUID.randomUUID() + "@example.com";
        MvcResult verified1 = registerAndVerify(email1);
        Cookie cookieUser1 = verified1.getResponse().getCookie("FG_AUTH");
        assertThat(cookieUser1).isNotNull();

        User user1 = userRepository.findByEmail(email1).orElseThrow();
        Account acc = new Account();
        acc.setUser(user1);
        acc.setName("Cash");
        acc.setCurrency("USD");
        acc.setInitialBalance(BigDecimal.valueOf(10));
        acc.setCurrentBalance(BigDecimal.valueOf(15));
        accountRepository.save(acc);

        String email2 = "prec2b-" + UUID.randomUUID() + "@example.com";
        MvcResult verified2 = registerAndVerify(email2);
        String tokenUser2 = accessToken(verified2);
        assertThat(tokenUser2).isNotBlank();

        MvcResult res = mockMvc.perform(get("/api/accounts/balance")
                        .cookie(cookieUser1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUser2))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(body.get("accounts").size()).isZero();
    }

    @Test
    void refreshTokenCannotAuthenticateProtectedEndpoint() throws Exception {
        MvcResult verified = registerAndVerify("prec3-" + UUID.randomUUID() + "@example.com");
        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(refreshCookie).isNotNull();
        String refresh = refreshCookie.getValue();
        assertThat(refresh).isNotBlank();

        mockMvc.perform(get("/api/accounts/balance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refresh))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshTokenInAuthorizationHeaderOverridesValidCookieAndBlocks() throws Exception {
        MvcResult verified = registerAndVerify("prec4-" + UUID.randomUUID() + "@example.com");
        Cookie accessCookie = verified.getResponse().getCookie("FG_AUTH");
        Cookie refreshCookie = verified.getResponse().getCookie("FG_REFRESH");
        assertThat(accessCookie).isNotNull();
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(get("/api/accounts/balance")
                        .cookie(accessCookie)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshCookie.getValue()))
                .andExpect(status().isForbidden());
    }

    private MvcResult registerAndVerify(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"StrongPass1!","fullName":"User","baseCurrency":"USD"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        return mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"654321"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String accessToken(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return body.get("token").asText();
    }
}

