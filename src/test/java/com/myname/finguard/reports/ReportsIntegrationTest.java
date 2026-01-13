package com.myname.finguard.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.FxRatesProvider;
import com.myname.finguard.common.service.MailService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.fx.cache-ttl-seconds=0",
        "app.crypto.cache-ttl-seconds=0"
})
class ReportsIntegrationTest {

    private static final Instant FIXED_AS_OF = Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @MockBean
    private FxRatesProvider fxRatesProvider;

    @MockBean
    private CryptoRatesProvider cryptoRatesProvider;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();

        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        FIXED_AS_OF,
                        Map.of(
                                "EUR", new BigDecimal("0.5"), // 1 USD = 0.5 EUR (1 EUR = 2 USD)
                                "RUB", new BigDecimal("100.0")
                        )
                ));

        when(cryptoRatesProvider.fetchLatest("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        FIXED_AS_OF,
                        List.of(
                                new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", new BigDecimal("10000"), BigDecimal.ZERO, List.of()),
                                new CryptoRatesProvider.CryptoRate("ETH", "Ethereum", new BigDecimal("2000"), BigDecimal.ZERO, List.of())
                        )
                ));
    }

    @Test
    @Transactional
    void summaryConvertsCurrenciesAndAggregatesIncomeExpenseNet() throws Exception {
        String token = registerAndVerify("rep-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        long usd = createAccount(token, "Main", "USD", "0");
        long eur = createAccount(token, "Euro", "EUR", "0");
        long btc = createAccount(token, "BTC", "BTC", "0");

        long food = findCategoryId(token, "Еда");
        long salary = createCategory(token, "Salary", "INCOME");

        createTransaction(token, usd, salary, "INCOME", "100.00", "2024-01-05T00:00:00Z");
        createTransaction(token, btc, salary, "INCOME", "0.01", "2024-01-05T12:00:00Z"); // 0.01 BTC = 100 USD
        createTransaction(token, usd, food, "EXPENSE", "20.00", "2024-01-06T00:00:00Z");
        createTransaction(token, eur, food, "EXPENSE", "10.00", "2024-01-06T12:00:00Z"); // 10 EUR = 20 USD

        String response = mockMvc.perform(get("/api/reports/summary")
                        .header("Authorization", "Bearer " + token)
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-01-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("baseCurrency").asText()).isEqualTo("USD");
        assertThat(json.get("income").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(json.get("expense").decimalValue()).isEqualByComparingTo("40.00");
        assertThat(json.get("net").decimalValue()).isEqualByComparingTo("160.00");
    }

    @Test
    @Transactional
    void byCategoryAggregatesIncomeAndExpenseSeparately() throws Exception {
        String token = registerAndVerify("rep-cat-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        long usd = createAccount(token, "Main", "USD", "0");
        long eur = createAccount(token, "Euro", "EUR", "0");
        long btc = createAccount(token, "BTC", "BTC", "0");

        long food = findCategoryId(token, "Еда");
        long salary = createCategory(token, "Salary", "INCOME");

        createTransaction(token, usd, salary, "INCOME", "100.00", "2024-01-05T00:00:00Z");
        createTransaction(token, btc, salary, "INCOME", "0.01", "2024-01-05T12:00:00Z");
        createTransaction(token, usd, food, "EXPENSE", "20.00", "2024-01-06T00:00:00Z");
        createTransaction(token, eur, food, "EXPENSE", "10.00", "2024-01-06T12:00:00Z");

        String response = mockMvc.perform(get("/api/reports/by-category")
                        .header("Authorization", "Bearer " + token)
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-01-31T23:59:59Z")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("baseCurrency").asText()).isEqualTo("USD");

        JsonNode expenses = json.get("expenses");
        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).get("categoryName").asText()).isEqualTo("Еда");
        assertThat(expenses.get(0).get("total").decimalValue()).isEqualByComparingTo("40.00");

        JsonNode incomes = json.get("incomes");
        assertThat(incomes).hasSize(1);
        assertThat(incomes.get(0).get("categoryName").asText()).isEqualTo("Salary");
        assertThat(incomes.get(0).get("total").decimalValue()).isEqualByComparingTo("200.00");
    }

    @Test
    @Transactional
    void cashFlowReturnsDailySeries() throws Exception {
        String token = registerAndVerify("rep-flow-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        long usd = createAccount(token, "Main", "USD", "0");
        long eur = createAccount(token, "Euro", "EUR", "0");
        long btc = createAccount(token, "BTC", "BTC", "0");

        long food = findCategoryId(token, "Еда");
        long salary = createCategory(token, "Salary", "INCOME");

        createTransaction(token, usd, salary, "INCOME", "100.00", "2024-01-05T00:00:00Z");
        createTransaction(token, btc, salary, "INCOME", "0.01", "2024-01-05T12:00:00Z");
        createTransaction(token, usd, food, "EXPENSE", "20.00", "2024-01-06T00:00:00Z");
        createTransaction(token, eur, food, "EXPENSE", "10.00", "2024-01-06T12:00:00Z");

        String response = mockMvc.perform(get("/api/reports/cash-flow")
                        .header("Authorization", "Bearer " + token)
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-01-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        JsonNode points = json.get("points");
        assertThat(points).hasSize(2);

        assertThat(points.get(0).get("date").asText()).isEqualTo("2024-01-05");
        assertThat(points.get(0).get("income").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(points.get(0).get("expense").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(points.get(0).get("net").decimalValue()).isEqualByComparingTo("200.00");

        assertThat(points.get(1).get("date").asText()).isEqualTo("2024-01-06");
        assertThat(points.get(1).get("income").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(points.get(1).get("expense").decimalValue()).isEqualByComparingTo("40.00");
        assertThat(points.get(1).get("net").decimalValue()).isEqualByComparingTo("-40.00");
    }

    @Test
    void anonymousIsForbidden() throws Exception {
        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidPeriodIsBadRequest() throws Exception {
        String token = registerAndVerify("rep-bad-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        mockMvc.perform(get("/api/reports/summary")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "nope"))
                .andExpect(status().isBadRequest());
    }

    private String registerAndVerify(String email, String password, String baseCurrency) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","fullName":"User","baseCurrency":"%s"}
                                """.formatted(email, password, baseCurrency)))
                .andExpect(status().isCreated());

        MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String code = extractCode(msg.body());

        MvcResult verify = mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk())
                .andReturn();

        String body = verify.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode json = objectMapper.readTree(body);
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

