package com.myname.finguard.common.dto;

import java.time.Instant;
import java.util.List;

public record CryptoRatesResponse(String baseCurrency, Instant asOf, List<CryptoRateDto> rates) {
}
