package com.myname.finguard.rules.dto;

import com.myname.finguard.rules.model.RuleType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateRuleRequest(
        @NotNull
        RuleType type,
        @NotNull
        Long categoryId,
        @NotNull
        BigDecimal limit,
        String currency,
        Boolean active
) {
}
