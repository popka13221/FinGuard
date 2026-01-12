package com.myname.finguard.accounts.dto;

import java.math.BigDecimal;

public record AccountDto(
        Long id,
        String name,
        String currency,
        BigDecimal initialBalance,
        BigDecimal currentBalance,
        boolean archived
) {
}

