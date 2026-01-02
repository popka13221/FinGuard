package com.myname.finguard.common.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface CryptoRatesProvider {

    CryptoRates fetchLatest(String baseCurrency);

    record CryptoRates(String baseCurrency, Instant asOf, List<CryptoRate> rates) {
    }

    record CryptoRate(String code, String name, BigDecimal price, BigDecimal changePct24h, List<BigDecimal> sparkline) {
    }
}
