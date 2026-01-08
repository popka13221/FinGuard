package com.myname.finguard.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBaseCurrencyRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3,5}$", message = "Base currency must be a 3-5 letter code")
        String baseCurrency
) {
}

