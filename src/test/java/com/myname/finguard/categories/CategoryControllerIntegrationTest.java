package com.myname.finguard.categories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.service.MailService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
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
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    void anonymousCannotListCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void listIncludesSeededBaseCategories() throws Exception {
        String token = registerVerifyAndLogin("cat-seed-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        String response = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode root = objectMapper.readTree(response);
        Set<String> names = new HashSet<>();
        root.forEach(node -> names.add(node.get("name").asText()));

        assertThat(names).contains("Еда", "Транспорт", "Подписки", "Инвестиции", "Прочее");
    }

    @Test
    @Transactional
    void createUpdateDeleteUserCategory() throws Exception {
        String token = registerVerifyAndLogin("cat-crud-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        String createResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Salary","type":"INCOME"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode created = objectMapper.readTree(createResponse);
        long id = created.get("id").asLong();
        assertThat(id).isPositive();
        assertThat(created.get("system").asBoolean()).isFalse();

        String updateResponse = mockMvc.perform(patch("/api/categories/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Salary Updated"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode updated = objectMapper.readTree(updateResponse);
        assertThat(updated.get("name").asText()).isEqualTo("Salary Updated");

        mockMvc.perform(delete("/api/categories/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String listResponse = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode list = objectMapper.readTree(listResponse);
        assertThat(list.findValuesAsText("name")).doesNotContain("Salary Updated");
    }

    @Test
    @Transactional
    void systemCategoryCannotBeDeleted() throws Exception {
        String token = registerVerifyAndLogin("cat-del-" + UUID.randomUUID() + "@example.com", "StrongPass1!");
        String response = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode root = objectMapper.readTree(response);
        JsonNode food = null;
        for (JsonNode node : root) {
            if ("Еда".equals(node.get("name").asText())) {
                food = node;
                break;
            }
        }
        assertThat(food).isNotNull();

        mockMvc.perform(delete("/api/categories/" + food.get("id").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private String registerVerifyAndLogin(String email, String password) throws Exception {
        String registerPayload = """
                {"email":"%s","password":"%s","fullName":"Test User","baseCurrency":"USD"}
                """.formatted(email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String verifyToken = extractCode(msg.body());

        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, verifyToken)))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode root = objectMapper.readTree(loginResponse);
        return root.get("token").asText();
    }

    private String extractCode(String body) {
        String decoded = URLDecoder.decode(body, StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("token=([0-9]{6})").matcher(decoded);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalStateException("No token found in mail body");
    }
}

