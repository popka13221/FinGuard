package com.myname.finguard.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardOverviewResponse(
        Instant asOf,
        String dataFreshness,
        Hero hero,
        Stats stats,
        GetStarted getStarted,
        List<TransactionPreview> transactionsPreview,
        List<WalletPreview> walletsPreview,
        List<UpcomingPaymentDto> upcomingPaymentsPreview,
        WalletIntelligence walletIntelligence
) {
    public record Hero(
            BigDecimal netWorth,
            String baseCurrency,
            BigDecimal delta7dPct,
            Instant updatedAt,
            boolean hasMeaningfulData
    ) {
    }

    public record Stats(
            BigDecimal income30d,
            BigDecimal spend30d,
            BigDecimal cashflow30d,
            BigDecimal debt,
            boolean hasMeaningfulData
    ) {
    }

    public record GetStarted(
            boolean visible,
            boolean connectAccount,
            boolean addTransaction,
            boolean importHistory
    ) {
    }

    public record TransactionPreview(
            Long id,
            Instant transactionDate,
            String description,
            String type,
            BigDecimal amount,
            String currency
    ) {
    }

    public record WalletPreview(
            Long id,
            String label,
            String network,
            String address,
            BigDecimal valueInBase,
            String baseCurrency
    ) {
    }

    public record WalletIntelligence(
            Long activeWalletId,
            String status,
            Integer progressPct,
            boolean partialReady,
            Instant updatedAt,
            String source
    ) {
    }
}
