package com.myname.finguard.accounts.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateAccountRequest(
        @Size(max = 255, message = "Account name must be at most 255 characters")
        String name,
        @Digits(integer = 17, fraction = 2, message = "Initial balance must have at most 2 decimal places")
        BigDecimal initialBalance,
        Boolean archived
) {
}

