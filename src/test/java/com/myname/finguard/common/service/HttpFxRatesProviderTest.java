package com.myname.finguard.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.myname.finguard.common.service.FxRatesProvider.FxRates;
import java.time.Instant;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpFxRatesProviderTest {

    @Test
    void fetchLatestParsesRatesAndUsesGuardWhenProvided() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        ExternalProviderGuard guard = mock(ExternalProviderGuard.class);
        org.mockito.Mockito.when(guard.execute(eq("fx-erapi"), eq(5), eq(1000L), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(3)).get());

        HttpFxRatesProvider provider = new HttpFxRatesProvider(builder, "https://fx.example/api/v6/latest/", guard, 5, 1000);

        server.expect(requestTo("https://fx.example/api/v6/latest/USD"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "result": "success",
                          "base_code": "USD",
                          "time_last_update_unix": 1704067200,
                          "rates": { "EUR": 0.9, "RUB": 90.0 }
                        }
                        """, MediaType.APPLICATION_JSON));

        FxRates rates = provider.fetchLatest("USD");

        assertThat(rates.baseCurrency()).isEqualTo("USD");
        assertThat(rates.asOf()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(rates.rates()).containsKeys("EUR", "RUB");
        assertThat(rates.rates().get("EUR")).isEqualByComparingTo("0.9");
        assertThat(rates.rates().get("RUB")).isEqualByComparingTo("90.0");

        verify(guard).execute(eq("fx-erapi"), eq(5), eq(1000L), any());
        server.verify();
    }

    @Test
    void fetchLatestRejectsNonSuccessResult() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpFxRatesProvider provider = new HttpFxRatesProvider(builder, "https://fx.example/api/v6/latest");

        server.expect(requestTo("https://fx.example/api/v6/latest/USD"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        { "result": "error", "base_code": "USD", "rates": { "EUR": 0.9 } }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.fetchLatest("USD"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FX provider error");

        server.verify();
    }

    @Test
    void fetchLatestRejectsEmptyRates() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpFxRatesProvider provider = new HttpFxRatesProvider(builder, "https://fx.example/api/v6/latest");

        server.expect(requestTo("https://fx.example/api/v6/latest/USD"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        { "result": "success", "base_code": "USD", "rates": {} }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.fetchLatest("USD"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Empty FX rates response");

        server.verify();
    }
}

