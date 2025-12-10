package com.yourname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourname.finguard.auth.repository.UserRepository;
import com.yourname.finguard.auth.repository.UserSessionRepository;
import com.yourname.finguard.auth.repository.UserTokenRepository;
import com.yourname.finguard.common.constants.ErrorCodes;
import com.yourname.finguard.security.RateLimiterService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.rate-limit.register-ip.limit=1",
        "app.security.rate-limit.register-ip.window-ms=60000",
        "app.security.rate-limit.register-email.limit=1",
        "app.security.rate-limit.register-email.window-ms=60000"
})
class RegisterRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private UserSessionRepository userSessionRepository;

    @AfterEach
    void cleanup() {
        userSessionRepository.deleteAll();
        userTokenRepository.deleteAll();
        userRepository.deleteAll();
        rateLimiterService.reset();
    }

    @Test
    void ipRateLimitBlocksSecondRegistration() throws Exception {
        String email1 = "limit-ip-1@" + UUID.randomUUID() + ".com";
        String email2 = "limit-ip-2@" + UUID.randomUUID() + ".com";

        register(email1, "StrongPass1!", "10.0.0.1")
                .andExpect(status().isCreated());

        MvcResult result = register(email2, "StrongPass1!", "10.0.0.1")
                .andExpect(status().isTooManyRequests())
                .andReturn();

        JsonNode error = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.RATE_LIMIT);
    }

    @Test
    void emailRateLimitTriggersBeforeEmailExistsCheck() throws Exception {
        String email = "limit-email@" + UUID.randomUUID() + ".com";

        register(email, "StrongPass1!", "10.0.0.2")
                .andExpect(status().isCreated());

        MvcResult second = register(email, "StrongPass1!", "10.0.0.3")
                .andExpect(status().isTooManyRequests())
                .andReturn();

        JsonNode error = objectMapper.readTree(second.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.RATE_LIMIT);
    }

    private org.springframework.test.web.servlet.ResultActions register(String email, String password, String ip) throws Exception {
        String payload = """
                {
                  "email": "%s",
                  "password": "%s",
                  "fullName": "User",
                  "baseCurrency": "USD"
                }
                """.formatted(email, password);
        RequestPostProcessor withIp = request -> {
            request.setRemoteAddr(ip);
            return request;
        };
        return mockMvc.perform(post("/api/auth/register")
                        .with(withIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload));
    }
}
