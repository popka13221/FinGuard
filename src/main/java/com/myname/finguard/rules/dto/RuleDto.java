package com.myname.finguard.rules.dto;

import com.myname.finguard.rules.model.RuleStatus;
import com.myname.finguard.rules.model.RuleType;
import java.math.BigDecimal;
import java.time.Instant;

public record RuleDto(
        Long id,
        RuleType type,
        RuleStatus status,
        Long categoryId,
        BigDecimal limit,
        String currency,
        Instant createdAt,
        Instant updatedAt,
        Instant lastTriggeredAt
) {
}
