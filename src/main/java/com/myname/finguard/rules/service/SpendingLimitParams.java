package com.myname.finguard.rules.service;

import java.math.BigDecimal;

public record SpendingLimitParams(
        Long categoryId,
        BigDecimal limit,
        String currency
) {
}
