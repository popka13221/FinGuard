package com.myname.finguard.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.myname.finguard.common.service.CryptoRatesProvider.CryptoRate;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpCryptoRatesProviderTest {

    @Test
    void fetchLatestParsesRatesForKnownAssets() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpCryptoRatesProvider provider = new HttpCryptoRatesProvider(builder, "https://crypto.example/api/v3");

        server.expect(requestTo(startsWith("https://crypto.example/api/v3/coins/markets")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("vs_currency", "usd"))
                .andExpect(queryParam("ids", "bitcoin,ethereum,solana"))
                .andExpect(queryParam("sparkline", "true"))
                .andExpect(queryParam("price_change_percentage", "24h"))
                .andRespond(withSuccess("""
                        [
                          {
                            "id": "bitcoin",
                            "symbol": "btc",
                            "name": "Bitcoin",
                            "current_price": 65000,
                            "price_change_percentage_24h": 1.2,
                            "sparkline_in_7d": { "price": [1, 2, 3] }
                          },
                          {
                            "id": "ethereum",
                            "symbol": "eth",
                            "name": "Ethereum",
                            "current_price": 3000,
                            "price_change_percentage_24h": -0.5,
                            "sparkline_in_7d": { "price": [4, 5] }
                          },
                          {
                            "id": "solana",
                            "symbol": "sol",
                            "name": "Solana",
                            "current_price": 120,
                            "price_change_percentage_24h": 0.0,
                            "sparkline_in_7d": { "price": [] }
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        CryptoRatesProvider.CryptoRates rates = provider.fetchLatest("USD");

        assertThat(rates.baseCurrency()).isEqualTo("USD");
        assertThat(rates.asOf()).isNotNull();
        assertThat(rates.rates()).extracting(CryptoRate::code).contains("BTC", "ETH", "SOL");

        Optional<CryptoRate> btc = rates.rates().stream().filter(rate -> "BTC".equals(rate.code())).findFirst();
        assertThat(btc).isPresent();
        assertThat(btc.get().price()).isEqualByComparingTo(new BigDecimal("65000"));
        assertThat(btc.get().changePct24h()).isEqualByComparingTo(new BigDecimal("1.2"));
        assertThat(btc.get().sparkline()).containsExactly(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"));

        server.verify();
    }
}

