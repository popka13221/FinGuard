package com.myname.finguard.common.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public interface FxRatesProvider {

    FxRates fetchLatest(String baseCurrency);

    record FxRates(String baseCurrency, Instant asOf, Map<String, BigDecimal> rates) {
    }
}

