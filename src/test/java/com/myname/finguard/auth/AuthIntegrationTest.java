package com.myname.finguard.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.service.MailService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MailService mailService;

    @Test
    @Transactional
    void registerAndLogin() throws Exception {
        String email = "demo@example.com";
        String password = "StrongPass1!";

        mailService.clearOutbox();

        String registerPayload = """
                {
                  "email": "%s",
                  "password": "%s",
                  "fullName": "Demo User",
                  "baseCurrency": "USD"
                }
                """.formatted(email, password);

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode regNode = objectMapper.readTree(registerResponse);
        assertThat(regNode.get("verificationRequired").asBoolean()).isTrue();

        // confirm email via code from mail
        com.myname.finguard.common.service.MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String verifyToken = extractCode(msg.body());
        assertThat(verifyToken).isNotBlank();

        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, verifyToken)))
                .andExpect(status().isOk());

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginNode = objectMapper.readTree(loginResponse);
        assertThat(loginNode.get("token").asText()).isNotBlank();
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        Matcher tokenParam = Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return URLDecoder.decode(tokenParam.group(1), StandardCharsets.UTF_8);
        }
        Matcher codeLine = Pattern.compile("(?i)code:\\s*([A-Za-z0-9-]{6,})").matcher(body);
        if (codeLine.find()) {
            return codeLine.group(1).trim();
        }
        Matcher digits = Pattern.compile("\\b\\d{6}\\b").matcher(body);
        if (digits.find()) {
            return digits.group().trim();
        }
        Matcher hex = Pattern.compile("\\b(?=[0-9a-fA-F-]*\\d)[0-9a-fA-F-]{6,}\\b").matcher(body);
        return hex.find() ? hex.group().trim() : "";
    }
}
