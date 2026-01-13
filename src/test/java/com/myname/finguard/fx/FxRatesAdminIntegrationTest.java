package com.myname.finguard.fx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.service.FxRatesProvider;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.fx.cache-ttl-seconds=0")
@Transactional
class FxRatesAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FxRatesProvider fxRatesProvider;

    @Test
    void upsertIsForbiddenForAnonymous() throws Exception {
        mockMvc.perform(post("/api/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"USD","asOf":"2024-01-01T00:00:00Z","rates":{"EUR":0.9}}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void upsertIsForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"USD","asOf":"2024-01-01T00:00:00Z","rates":{"EUR":0.9}}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsertStoresRatesAndGetReturnsFromDbEvenIfProviderFails() throws Exception {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenThrow(new RuntimeException("no network in test"));

        Instant asOf = Instant.parse("2024-01-01T00:00:00Z");
        String upsertResponse = mockMvc.perform(post("/api/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"USD","asOf":"%s","rates":{"EUR":0.9,"RUB":90.0}}
                                """.formatted(asOf)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode upsert = objectMapper.readTree(upsertResponse);
        assertThat(upsert.get("baseCurrency").asText()).isEqualTo("USD");
        assertThat(upsert.get("asOf").asText()).isEqualTo(asOf.toString());
        assertThat(upsert.get("rates").get("EUR").decimalValue()).isEqualByComparingTo("0.9");
        assertThat(upsert.get("rates").get("RUB").decimalValue()).isEqualByComparingTo("90.0");

        String getResponse = mockMvc.perform(get("/api/fx/rates").param("base", "USD"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(getResponse);
        assertThat(json.get("baseCurrency").asText()).isEqualTo("USD");
        assertThat(json.get("asOf").asText()).isEqualTo(asOf.toString());
        assertThat(json.get("rates").get("EUR").decimalValue()).isEqualByComparingTo("0.9");
        assertThat(json.get("rates").get("RUB").decimalValue()).isEqualByComparingTo("90.0");
        List<String> keys = new ArrayList<>();
        json.get("rates").fieldNames().forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder("EUR", "RUB");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsertValidatesCurrencyAndRates() throws Exception {
        mockMvc.perform(post("/api/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"BTC","asOf":"2024-01-01T00:00:00Z","rates":{"USD":10000}}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"USD","asOf":"2024-01-01T00:00:00Z","rates":{"BTC":10000}}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"baseCurrency":"USD","asOf":"2024-01-01T00:00:00Z","rates":{"EUR":0}}
                                """))
                .andExpect(status().isBadRequest());
    }
}
