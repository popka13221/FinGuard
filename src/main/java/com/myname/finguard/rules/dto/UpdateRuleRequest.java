package com.myname.finguard.rules.dto;

import java.math.BigDecimal;

public record UpdateRuleRequest(
        Long categoryId,
        BigDecimal limit,
        String currency,
        Boolean active
) {
}
