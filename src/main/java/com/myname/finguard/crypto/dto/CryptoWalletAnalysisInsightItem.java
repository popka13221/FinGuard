package com.myname.finguard.crypto.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoWalletAnalysisInsightItem(
        String type,
        String title,
        BigDecimal value,
        String unit,
        String currency,
        String label,
        BigDecimal avgAmount,
        Instant nextEstimatedChargeAt,
        BigDecimal confidence,
        Instant asOf,
        boolean synthetic
) {
}
