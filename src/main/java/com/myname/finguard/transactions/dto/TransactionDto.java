package com.myname.finguard.transactions.dto;

import com.myname.finguard.transactions.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        Long id,
        Long accountId,
        Long categoryId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        Instant transactionDate,
        String description
) {
}

