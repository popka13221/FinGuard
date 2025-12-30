package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHeadersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void securityHeadersArePresentOnRootAndCsrfEndpoint() throws Exception {
        assertSecurityHeaders(mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn(), false);
        assertSecurityHeaders(mockMvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn(), false);
    }

    @Test
    void hstsIsSetOnSecureRequests() throws Exception {
        MvcResult res = mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andReturn();
        assertSecurityHeaders(res, true);
    }

    private void assertSecurityHeaders(MvcResult res, boolean expectHsts) {
        assertThat(res.getResponse().getHeader("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; font-src 'self' data:; script-src 'self'; connect-src 'self'");
        assertThat(res.getResponse().getHeader("Referrer-Policy")).isEqualTo("same-origin");
        assertThat(res.getResponse().getHeader("Permissions-Policy"))
                .isEqualTo("geolocation=(), microphone=(), camera=()");

        assertThat(res.getResponse().getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(res.getResponse().getHeader("X-Frame-Options")).isEqualTo("DENY");

        String hsts = res.getResponse().getHeader("Strict-Transport-Security");
        if (expectHsts) {
            assertThat(hsts).isNotBlank();
            assertThat(hsts).contains("max-age=31536000");
            assertThat(hsts).containsIgnoringCase("includesubdomains");
        } else {
            // HSTS is only set for secure (HTTPS) requests.
            assertThat(hsts).isNull();
        }
    }
}
