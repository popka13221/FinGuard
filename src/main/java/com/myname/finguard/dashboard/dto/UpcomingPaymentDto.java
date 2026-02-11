package com.myname.finguard.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UpcomingPaymentDto(
        String id,
        String title,
        BigDecimal amount,
        String currency,
        Instant dueAt,
        BigDecimal confidence,
        String source
) {
}
