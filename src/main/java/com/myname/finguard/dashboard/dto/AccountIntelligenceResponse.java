package com.myname.finguard.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AccountIntelligenceResponse(
        Instant asOf,
        String source,
        Summary summary,
        Series series,
        List<WalletAllocationItem> walletsAllocation,
        List<AccountInsightDto> insights,
        Status status,
        boolean hasMeaningfulData
) {
    public record Summary(
            BigDecimal netWorth,
            String baseCurrency,
            BigDecimal delta7dPct,
            BigDecimal income30d,
            BigDecimal spend30d,
            BigDecimal cashflow30d,
            BigDecimal debt,
            boolean hasMeaningfulData
    ) {
    }

    public record Series(
            String window,
            String metric,
            List<SeriesPoint> points,
            boolean compact,
            boolean hasMeaningfulData
    ) {
    }

    public record SeriesPoint(
            Instant at,
            BigDecimal valueInBase
    ) {
    }

    public record WalletAllocationItem(
            Long walletId,
            String label,
            String network,
            String address,
            BigDecimal amount,
            String asset,
            BigDecimal valueInBase,
            BigDecimal sharePct,
            boolean hasMeaningfulData
    ) {
    }

    public record Status(
            String status,
            Integer progressPct,
            boolean partialReady,
            Instant updatedAt,
            String source,
            Integer etaSeconds,
            String lastSuccessfulStage
    ) {
    }
}
