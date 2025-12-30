package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.csrf.enabled=true"
})
class CsrfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postWithoutXsrfHeaderIsForbiddenWhenCookiePresent() throws Exception {
        MvcResult csrfRes = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode csrfBody = objectMapper.readTree(csrfRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = csrfBody.get("token").asText();
        Cookie cookie = csrfRes.getResponse().getCookie("XSRF-TOKEN");

        assertThat(token).isNotBlank();
        assertThat(cookie).isNotNull();

        MvcResult blocked = mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andReturn();

        JsonNode error = objectMapper.readTree(blocked.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100001");
    }

    @Test
    void postWithXsrfHeaderPasses() throws Exception {
        MvcResult csrfRes = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode csrfBody = objectMapper.readTree(csrfRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = csrfBody.get("token").asText();
        Cookie cookie = csrfRes.getResponse().getCookie("XSRF-TOKEN");

        assertThat(token).isNotBlank();
        assertThat(cookie).isNotNull();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookie)
                        .header("X-XSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void postWithWrongXsrfHeaderIsForbidden() throws Exception {
        MvcResult csrfRes = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode csrfBody = objectMapper.readTree(csrfRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String token = csrfBody.get("token").asText();
        Cookie cookie = csrfRes.getResponse().getCookie("XSRF-TOKEN");

        assertThat(token).isNotBlank();
        assertThat(cookie).isNotNull();

        MvcResult blocked = mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookie)
                        .header("X-XSRF-TOKEN", token + "x")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andReturn();

        JsonNode error = objectMapper.readTree(blocked.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(error.get("code").asText()).isEqualTo("100001");
    }
}
