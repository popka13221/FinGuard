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
import java.time.temporal.ChronoUnit;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionListSecurityIntegrationTest {

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
    void listSupportsLimitAndOrdersByDateDesc() throws Exception {
        String token = registerVerifyAndLogin("tx-list-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        long account = createAccount(token, "Main", "USD", "0");
        long category = createCategory(token, "Food", "EXPENSE");

        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        for (int i = 0; i < 5; i++) {
            createTransaction(token, account, category, "EXPENSE", "1.00", now.minus(i, ChronoUnit.DAYS).toString());
        }

        Instant from = now.minus(30, ChronoUnit.DAYS);
        MvcResult res = mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("from", from.toString())
                        .param("to", now.toString())
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(body.isArray()).isTrue();
        assertThat(body).hasSize(2);
        Instant first = Instant.parse(body.get(0).get("transactionDate").asText());
        Instant second = Instant.parse(body.get(1).get("transactionDate").asText());
        assertThat(first).isAfterOrEqualTo(second);
    }

    @Test
    @Transactional
    void listRejectsInvalidLimit() throws Exception {
        String token = registerVerifyAndLogin("tx-limit-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("limit", "9999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void cannotReadOrDeleteOtherUsersTransactions() throws Exception {
        String tokenA = registerVerifyAndLogin("tx-a-" + UUID.randomUUID() + "@example.com", "StrongPass1!");
        String tokenB = registerVerifyAndLogin("tx-b-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        long account = createAccount(tokenA, "Main", "USD", "0");
        long category = createCategory(tokenA, "Food", "EXPENSE");
        long txId = createTransaction(tokenA, account, category, "EXPENSE", "3.00", Instant.now().toString());

        mockMvc.perform(get("/api/transactions/{id}", txId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/transactions/{id}", txId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void cannotCreateTransactionOnArchivedAccount() throws Exception {
        String token = registerVerifyAndLogin("tx-arch-" + UUID.randomUUID() + "@example.com", "StrongPass1!");

        long account = createAccount(token, "Main", "USD", "0");
        long category = createCategory(token, "Food", "EXPENSE");

        mockMvc.perform(patch("/api/accounts/{id}", account)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"archived":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":%d,"categoryId":%d,"type":"EXPENSE","amount":1.00,"transactionDate":"%s"}
                                """.formatted(account, category, Instant.now())))
                .andExpect(status().isBadRequest());
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
                                {"accountId":%d,"categoryId":%d,"type":"%s","amount":%s,"transactionDate":"%s"}
                                """.formatted(accountId, categoryId, type, amount, transactionDate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode created = objectMapper.readTree(response);
        return created.get("id").asLong();
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

