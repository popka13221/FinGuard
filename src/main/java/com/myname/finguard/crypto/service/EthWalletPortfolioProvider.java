package com.myname.finguard.crypto.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface EthWalletPortfolioProvider {

    EthWalletPortfolio fetchLatest(String addressNormalized);

    record EthWalletPortfolio(
            String address,
            Instant asOf,
            BigDecimal tokenValueUsd,
            List<TokenHolding> topTokens
    ) {
    }

    record TokenHolding(
            String contractAddress,
            String symbol,
            BigDecimal amount,
            BigDecimal priceUsd,
            BigDecimal valueUsd
    ) {
    }
}

