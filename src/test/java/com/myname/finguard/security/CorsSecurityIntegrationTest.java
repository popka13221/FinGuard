package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.cors.allowed-origins=http://localhost:5173"
})
class CorsSecurityIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowedOriginGetsCorsHeaders() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/currencies")
                        .header("Origin", ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(res.getResponse().getHeader("Access-Control-Allow-Origin")).isEqualTo(ALLOWED_ORIGIN);
        assertThat(res.getResponse().getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(res.getResponse().getHeader("Access-Control-Allow-Origin")).isNotEqualTo("*");
    }

    @Test
    void unknownOriginDoesNotGetCorsHeaders() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/currencies")
                        .header("Origin", "http://evil.example"))
                .andExpect(status().isForbidden())
                .andReturn();

        assertThat(res.getResponse().getHeader("Access-Control-Allow-Origin")).isNull();
        assertThat(res.getResponse().getHeader("Access-Control-Allow-Credentials")).isNull();
    }

    @Test
    void preflightContainsAllowedHeadersAndMethods() throws Exception {
        MvcResult res = mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type,X-XSRF-TOKEN"))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isIn(200, 204);

        String allowOrigin = res.getResponse().getHeader("Access-Control-Allow-Origin");
        assertThat(allowOrigin).isEqualTo(ALLOWED_ORIGIN);

        String allowHeaders = res.getResponse().getHeader("Access-Control-Allow-Headers");
        assertThat(allowHeaders).isNotBlank();
        assertThat(allowHeaders).containsIgnoringCase("authorization");

        String allowMethods = res.getResponse().getHeader("Access-Control-Allow-Methods");
        assertThat(allowMethods).isNotBlank();
        assertThat(allowMethods).contains("POST");
    }
}
