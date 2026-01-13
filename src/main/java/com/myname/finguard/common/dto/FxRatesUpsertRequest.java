package com.myname.finguard.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record FxRatesUpsertRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3,5}$", message = "Base currency must be a 3-5 letter code")
        String baseCurrency,
        @NotNull
        Instant asOf,
        @NotNull
        Map<String, BigDecimal> rates
) {
}

