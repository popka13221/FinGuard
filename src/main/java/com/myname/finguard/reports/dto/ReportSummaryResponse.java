package com.myname.finguard.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportSummaryResponse(
        String baseCurrency,
        Instant from,
        Instant to,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}

