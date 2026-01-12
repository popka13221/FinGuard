package com.myname.finguard.transactions.dto;

import com.myname.finguard.transactions.model.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record UpdateTransactionRequest(
        Long accountId,
        Long categoryId,
        TransactionType type,
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,
        Instant transactionDate,
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description
) {
}

