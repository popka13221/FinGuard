package com.myname.finguard.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CashFlowResponse(
        String baseCurrency,
        Instant from,
        Instant to,
        List<CashFlowPoint> points
) {
    public record CashFlowPoint(LocalDate date, BigDecimal income, BigDecimal expense, BigDecimal net) {
    }
}

