package com.myname.finguard.notifications.dto;

import java.time.Instant;
import java.time.LocalDate;

public record NotificationDto(
        Long id,
        Long ruleId,
        String message,
        LocalDate periodStart,
        Instant createdAt,
        Instant readAt
) {
}
