package com.myname.finguard.common.dto;

import java.math.BigDecimal;
import java.util.List;

public record CryptoRateDto(
        String code,
        String name,
        BigDecimal price,
        BigDecimal changePct24h,
        List<BigDecimal> sparkline
) {
}
