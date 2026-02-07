package com.myname.finguard.crypto.dto;

import java.time.Instant;
import java.util.List;

public record CryptoWalletAnalysisInsightsResponse(
        String baseCurrency,
        List<CryptoWalletAnalysisInsightItem> insights,
        Instant asOf
) {
}
