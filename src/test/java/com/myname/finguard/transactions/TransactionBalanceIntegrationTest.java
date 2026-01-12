package com.myname.finguard.transactions;

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
import java.time.Instant;
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
class TransactionBalanceIntegrationTest {

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
    @Transactional
    void createUpdateMoveDeleteRecalculatesAccountBalance() throws Exception {
        String token = registerVerifyAndLogin("tx-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        long account1 = createAccount(token, "Main", "USD", "100.00");
        long account2 = createAccount(token, "Cash", "USD", "50.00");
        long foodCategoryId = findCategoryId(token, "Еда");
        long salaryCategoryId = createCategory(token, "Salary", "INCOME");

        String date = Instant.now().toString();
        long txId = createTransaction(token, account1, foodCategoryId, "EXPENSE", "10.00", date);
        assertThat(balanceOf(token, account1)).isEqualByComparingTo("90.00");

        patchTransaction(token, txId, """
                {"amount":20.00}
                """);
        assertThat(balanceOf(token, account1)).isEqualByComparingTo("80.00");

        patchTransaction(token, txId, """
                {"accountId":%d}
                """.formatted(account2));
        assertThat(balanceOf(token, account1)).isEqualByComparingTo("100.00");
        assertThat(balanceOf(token, account2)).isEqualByComparingTo("30.00");

        patchTransaction(token, txId, """
                {"type":"INCOME","categoryId":%d}
                """.formatted(salaryCategoryId));
        assertThat(balanceOf(token, account2)).isEqualByComparingTo("70.00");

        mockMvc.perform(delete("/api/transactions/" + txId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertThat(balanceOf(token, account2)).isEqualByComparingTo("50.00");
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

    private long createCategory(String token, String name, String type) throws Exception {
        String response = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","type":"%s"}
                                """.formatted(name, type)))
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

    private long createTransaction(
            String token,
            long accountId,
            long categoryId,
            String type,
            String amount,
            String transactionDate
    ) throws Exception {
        String response = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":%d,"categoryId":%d,"type":"%s","amount":%s,"transactionDate":"%s","description":"Lunch"}
                                """.formatted(accountId, categoryId, type, amount, transactionDate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode created = objectMapper.readTree(response);
        return created.get("id").asLong();
    }

    private void patchTransaction(String token, long txId, String payload) throws Exception {
        mockMvc.perform(patch("/api/transactions/" + txId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    private java.math.BigDecimal balanceOf(String token, long accountId) throws Exception {
        String response = mockMvc.perform(get("/api/accounts/balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(response);
        for (JsonNode acc : root.get("accounts")) {
            if (acc.get("id").asLong() == accountId) {
                return acc.get("balance").decimalValue();
            }
        }
        throw new IllegalStateException("Account not found in balance response: " + accountId);
    }

    private String registerVerifyAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"Test User","baseCurrency":"USD"}
                                """.formatted(email, password)))
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

