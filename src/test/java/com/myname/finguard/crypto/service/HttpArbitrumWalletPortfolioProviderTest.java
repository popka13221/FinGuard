package com.myname.finguard.crypto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpArbitrumWalletPortfolioProviderTest {

    @Test
    void computesUsdTokenValueFromBlockscoutAndLlamaPrices() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpArbitrumWalletPortfolioProvider provider = new HttpArbitrumWalletPortfolioProvider(
                builder,
                "https://blockscout.example",
                "https://prices.example"
        );

        String address = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";
        server.expect(requestTo("https://blockscout.example/api?module=account&action=tokenlist&address=" + address))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "message": "OK",
                          "result": [
                            {
                              "balance": "2000000",
                              "contractAddress": "0x0000000000000000000000000000000000000001",
                              "decimals": "6",
                              "symbol": "USDC",
                              "type": "ERC-20"
                            },
                            {
                              "balance": "1000000000000000000",
                              "contractAddress": "0x0000000000000000000000000000000000000002",
                              "decimals": "18",
                              "symbol": "ABC",
                              "type": "ERC-20"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo(Matchers.containsString("https://prices.example/prices/current/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "coins": {
                            "arbitrum:0x0000000000000000000000000000000000000001": { "price": 1 },
                            "arbitrum:0x0000000000000000000000000000000000000002": { "price": 2 }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        ArbitrumWalletPortfolioProvider.ArbitrumWalletPortfolio portfolio = provider.fetchLatest(address);

        assertThat(portfolio.address()).isEqualTo(address);
        assertThat(portfolio.tokenValueUsd()).isEqualByComparingTo("4.000000000000");
        assertThat(portfolio.topTokens()).hasSize(2);
        assertThat(portfolio.topTokens().get(0).symbol()).isEqualTo("USDC");
        assertThat(portfolio.topTokens().get(0).amount()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(portfolio.topTokens().get(1).symbol()).isEqualTo("ABC");
        assertThat(portfolio.topTokens().get(1).amount()).isEqualByComparingTo(new BigDecimal("1"));
        server.verify();
    }
}
