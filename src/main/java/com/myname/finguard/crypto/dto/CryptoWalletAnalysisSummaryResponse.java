package com.myname.finguard.crypto.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CryptoWalletAnalysisSummaryResponse(
        BigDecimal totalValueInBase,
        String baseCurrency,
        BigDecimal delta24hPct,
        BigDecimal delta7dPct,
        List<AllocationItem> allocation,
        Instant asOf,
        boolean synthetic
) {
    public record AllocationItem(
            String code,
            BigDecimal valueInBase,
            BigDecimal sharePct
    ) {
    }
}
