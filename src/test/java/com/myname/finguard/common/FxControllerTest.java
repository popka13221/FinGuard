package com.myname.finguard.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.service.FxRatesProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.fx.cache-ttl-seconds=0")
class FxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FxRatesProvider fxRatesProvider;

    @Test
    void returnsRatesForBaseCurrency() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of(
                                "EUR", new BigDecimal("0.9"),
                                "RUB", new BigDecimal("90.0")
                        )
                ));

        String response = mockMvc.perform(get("/api/fx/rates").param("base", "usd"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("baseCurrency").asText()).isEqualTo("USD");
        assertThat(json.get("rates").get("EUR").decimalValue()).isEqualByComparingTo("0.9");
        assertThat(json.get("rates").get("RUB").decimalValue()).isEqualByComparingTo("90.0");
    }

    @Test
    void filtersQuotesWhenRequested() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of(
                                "EUR", new BigDecimal("0.9"),
                                "RUB", new BigDecimal("90.0")
                        )
                ));

        String response = mockMvc.perform(get("/api/fx/rates")
                        .param("base", "USD")
                        .param("quote", "EUR"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        JsonNode rates = json.get("rates");
        assertThat(rates.has("EUR")).isTrue();
        assertThat(rates.has("RUB")).isFalse();
    }

    @Test
    void returnsBaseRateWhenQuoteMatchesBase() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of("EUR", new BigDecimal("0.9"))
                ));

        String response = mockMvc.perform(get("/api/fx/rates")
                        .param("base", "USD")
                        .param("quote", "USD"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        JsonNode rates = json.get("rates");
        assertThat(rates.has("USD")).isTrue();
        assertThat(rates.get("USD").decimalValue()).isEqualByComparingTo("1.0");
    }

    @Test
    void rejectsUnsupportedBaseCurrency() throws Exception {
        String response = mockMvc.perform(get("/api/fx/rates")
                        .param("base", "ABC"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo("400000");
    }

    @Test
    void rejectsUnsupportedQuoteCurrency() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of("EUR", new BigDecimal("0.9"))
                ));

        String response = mockMvc.perform(get("/api/fx/rates")
                        .param("base", "USD")
                        .param("quote", "ABC"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo("400000");
    }

    @Test
    void rejectsMissingQuoteRate() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of("EUR", new BigDecimal("0.9"))
                ));

        String response = mockMvc.perform(get("/api/fx/rates")
                        .param("base", "USD")
                        .param("quote", "RUB"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo("400000");
    }
}
