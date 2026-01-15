package com.myname.finguard.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.notifications.dto.BulkMarkReadRequest;
import com.myname.finguard.rules.service.RuleEvaluationService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.task.scheduling.enabled=false")
class RulesNotificationsIntegrationTest {

    private static final Instant FIXED_NOW = Instant.parse("2024-01-15T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private RuleEvaluationService ruleEvaluationService;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    @Transactional
    void spendingRuleCreatesNotificationAndDedupes() throws Exception {
        String token = registerAndVerify("rules-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        long accountId = createAccount(token, "Main", "USD", "0");
        long categoryId = findCategoryId(token, "Еда");

        createTransaction(token, accountId, categoryId, "EXPENSE", "120.00", FIXED_NOW.toString());
        long ruleId = createRule(token, categoryId, "100.00", "USD", true);
        assertThat(ruleId).isPositive();

        ruleEvaluationService.evaluateActiveRules(FIXED_NOW);

        List<JsonNode> notifications = listNotifications(token);
        assertThat(notifications).hasSize(1);
        long notificationId = notifications.get(0).get("id").asLong();
        assertThat(notificationId).isPositive();

        long unread = unreadCount(token);
        assertThat(unread).isEqualTo(1L);

        JsonNode updated = updateNotification(token, notificationId, true);
        assertThat(updated.get("readAt").isNull()).isFalse();

        long unreadAfter = unreadCount(token);
        assertThat(unreadAfter).isZero();

        ruleEvaluationService.evaluateActiveRules(FIXED_NOW);
        List<JsonNode> afterDedup = listNotifications(token);
        assertThat(afterDedup).hasSize(1);
    }

    @Test
    @Transactional
    void bulkMarkReadUpdatesAll() throws Exception {
        String token = registerAndVerify("rules-bulk-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        long accountId = createAccount(token, "Main", "USD", "0");
        long categoryId = findCategoryId(token, "Еда");

        createTransaction(token, accountId, categoryId, "EXPENSE", "120.00", FIXED_NOW.toString());
        createRule(token, categoryId, "80.00", "USD", true);
        createRule(token, categoryId, "100.00", "USD", true);

        ruleEvaluationService.evaluateActiveRules(FIXED_NOW);

        List<JsonNode> notifications = listNotifications(token);
        assertThat(notifications).hasSize(2);
        List<Long> ids = new ArrayList<>();
        notifications.forEach(node -> ids.add(node.get("id").asLong()));

        int updated = bulkMarkRead(token, ids);
        assertThat(updated).isEqualTo(2);
        assertThat(unreadCount(token)).isZero();
    }

    @Test
    @Transactional
    void rulesAreScopedToUser() throws Exception {
        String tokenA = registerAndVerify("rules-a-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String tokenB = registerAndVerify("rules-b-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        long categoryId = findCategoryId(tokenA, "Еда");
        long ruleId = createRule(tokenA, categoryId, "50.00", "USD", true);

        mockMvc.perform(get("/api/rules/" + ruleId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest());
    }

    private String registerAndVerify(String email, String password, String baseCurrency) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"Test User","baseCurrency":"%s"}
                                """.formatted(email, password, baseCurrency)))
                .andExpect(status().isCreated());

        MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String code = extractCode(msg.body());

        String response = mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private long createAccount(String token, String name, String currency, String initialBalance) throws Exception {
        String response = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","currency":"%s","initialBalance":%s}
                                """.formatted(name, currency, initialBalance)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode created = objectMapper.readTree(response);
        return created.get("id").asLong();
    }

    private long findCategoryId(String token, String name) throws Exception {
        String response = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);
        for (JsonNode node : root) {
            if (name.equals(node.get("name").asText())) {
                return node.get("id").asLong();
            }
        }
        throw new IllegalStateException("Category not found: " + name);
    }

    private void createTransaction(
            String token,
            long accountId,
            long categoryId,
            String type,
            String amount,
            String transactionDate
    ) throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":%d,"categoryId":%d,"type":"%s","amount":%s,"transactionDate":"%s","description":"Test"}
                                """.formatted(accountId, categoryId, type, amount, transactionDate)))
                .andExpect(status().isCreated());
    }

    private long createRule(String token, long categoryId, String limit, String currency, boolean active) throws Exception {
        String response = mockMvc.perform(post("/api/rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"SPENDING_LIMIT_CATEGORY_MONTHLY","categoryId":%d,"limit":%s,"currency":"%s","active":%s}
                                """.formatted(categoryId, limit, currency, active)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode created = objectMapper.readTree(response);
        return created.get("id").asLong();
    }

    private List<JsonNode> listNotifications(String token) throws Exception {
        String response = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);
        List<JsonNode> items = new ArrayList<>();
        root.forEach(items::add);
        return items;
    }

    private long unreadCount(String token) throws Exception {
        String response = mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);
        return root.get("count").asLong();
    }

    private JsonNode updateNotification(String token, long notificationId, boolean read) throws Exception {
        String response = mockMvc.perform(patch("/api/notifications/" + notificationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"read":%s}
                                """.formatted(read)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response);
    }

    private int bulkMarkRead(String token, List<Long> ids) throws Exception {
        String response = mockMvc.perform(post("/api/notifications/mark-read")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BulkMarkReadRequest(ids))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);
        return root.get("updated").asInt();
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        java.util.regex.Matcher tokenParam = java.util.regex.Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return java.net.URLDecoder.decode(tokenParam.group(1), StandardCharsets.UTF_8);
        }
        java.util.regex.Matcher digits = java.util.regex.Pattern.compile("\\b\\d{6}\\b").matcher(body);
        return digits.find() ? digits.group().trim() : "";
    }
}
