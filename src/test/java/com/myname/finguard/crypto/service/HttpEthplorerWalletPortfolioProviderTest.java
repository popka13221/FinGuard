package com.myname.finguard.crypto.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class HttpEthplorerWalletPortfolioProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parseTokensUsesStablecoinFallbackAndIgnoresTokensWithoutPrice() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {
                  "tokens": [
                    {
                      "tokenInfo": {
                        "address": "0xdac17f958d2ee523a2206206994597c13d831ec7",
                        "symbol": "USDT",
                        "decimals": "6",
                        "price": false
                      },
                      "rawBalance": "1000000"
                    },
                    {
                      "tokenInfo": {
                        "address": "0x0000000000000000000000000000000000000002",
                        "symbol": "ABC",
                        "decimals": "18",
                        "price": { "rate": 2, "currency": "USD" }
                      },
                      "rawBalance": "500000000000000000"
                    },
                    {
                      "tokenInfo": {
                        "address": "0x0000000000000000000000000000000000000003",
                        "symbol": "NOPRICE",
                        "decimals": "18",
                        "price": false
                      },
                      "rawBalance": "1000000000000000000"
                    }
                  ]
                }
                """);

        HttpEthplorerWalletPortfolioProvider.ParsedTokens parsed = HttpEthplorerWalletPortfolioProvider.parseTokens(payload);

        assertThat(parsed.totalUsd()).isEqualByComparingTo("2.000000000000");
        assertThat(parsed.topTokens()).hasSize(2);
        assertThat(parsed.topTokens().stream().map(EthWalletPortfolioProvider.TokenHolding::symbol))
                .containsExactlyInAnyOrder("USDT", "ABC");
        BigDecimal usdtValue = parsed.topTokens().stream()
                .filter(t -> "USDT".equals(t.symbol()))
                .map(EthWalletPortfolioProvider.TokenHolding::valueUsd)
                .findFirst()
                .orElseThrow();
        assertThat(usdtValue).isEqualByComparingTo(new BigDecimal("1.000000000000"));
    }
}
