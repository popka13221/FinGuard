package com.myname.finguard.dashboard.dto;

import java.math.BigDecimal;

public record AccountInsightDto(
        String type,
        String title,
        BigDecimal value,
        String unit,
        String meta,
        BigDecimal confidence,
        boolean isActionable
) {
}
