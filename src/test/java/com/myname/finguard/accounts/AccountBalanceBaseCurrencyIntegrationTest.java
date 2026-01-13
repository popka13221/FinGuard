package com.myname.finguard.accounts;

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
import com.myname.finguard.fx.service.FxRateService;
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
class AccountBalanceBaseCurrencyIntegrationTest {

    private static final Instant FIXED_AS_OF = Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private FxRateService fxRateService;

    @MockBean
    private FxRatesProvider fxRatesProvider;

    @MockBean
    private CryptoRatesProvider cryptoRatesProvider;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    @Transactional
    void returnsTotalInBaseForMixedFiatAndCryptoBalances() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        FIXED_AS_OF,
                        Map.of("EUR", new BigDecimal("0.5"))
                ));
        when(cryptoRatesProvider.fetchLatest("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        FIXED_AS_OF,
                        List.of(new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", new BigDecimal("10000"), BigDecimal.ZERO, List.of()))
                ));

        String token = registerAndVerify("bal-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        createAccount(token, "Main", "USD", "100.00");
        createAccount(token, "Euro", "EUR", "10.00"); // 10 EUR = 20 USD when 1 USD = 0.5 EUR
        createAccount(token, "BTC", "BTC", "0.01"); // 0.01 BTC = 100 USD when BTC = 10000 USD

        String response = mockMvc.perform(get("/api/accounts/balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("baseCurrency").asText()).isEqualTo("USD");
        assertThat(json.get("totalInBase").decimalValue()).isEqualByComparingTo("220.00");
    }

    @Test
    @Transactional
    void usesStoredFxRatesWhenProviderUnavailable() throws Exception {
        fxRateService.upsertSnapshot("USD", FIXED_AS_OF, Map.of("EUR", new BigDecimal("0.5")));
        when(fxRatesProvider.fetchLatest("USD"))
                .thenThrow(new RuntimeException("no network"));

        String token = registerAndVerify("bal-stored-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        createAccount(token, "Main", "USD", "100.00");
        createAccount(token, "Euro", "EUR", "10.00");

        String response = mockMvc.perform(get("/api/accounts/balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("baseCurrency").asText()).isEqualTo("USD");
        assertThat(json.get("totalInBase").decimalValue()).isEqualByComparingTo("120.00");
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

    private void createAccount(String token, String name, String currency, String initialBalance) throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","currency":"%s","initialBalance":%s}
                                """.formatted(name, currency, initialBalance)))
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

