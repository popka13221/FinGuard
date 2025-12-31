package com.myname.finguard.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record FxRatesResponse(String baseCurrency, Instant asOf, Map<String, BigDecimal> rates) {
}
