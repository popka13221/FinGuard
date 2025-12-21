package com.myname.finguard.accounts.dto;

import java.math.BigDecimal;
import java.util.List;

public record UserBalanceResponse(
        List<AccountBalance> accounts,
        List<CurrencyBalance> totalsByCurrency
) {
    public record AccountBalance(Long id, String name, String currency, BigDecimal balance, boolean archived) {
    }

    public record CurrencyBalance(String currency, BigDecimal total) {
    }
}
