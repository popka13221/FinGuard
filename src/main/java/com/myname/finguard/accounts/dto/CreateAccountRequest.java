package com.myname.finguard.accounts.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank
        @Size(max = 255, message = "Account name must be at most 255 characters")
        String name,
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3,5}$", message = "Currency must be a 3-5 letter code")
        String currency,
        @Digits(integer = 17, fraction = 2, message = "Initial balance must have at most 2 decimal places")
        BigDecimal initialBalance
) {
}

