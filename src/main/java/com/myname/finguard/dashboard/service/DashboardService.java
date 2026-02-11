package com.myname.finguard.dashboard.service;

import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.service.AccountService;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.dto.CryptoWalletSummaryResponse;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJobStatus;
import com.myname.finguard.crypto.model.WalletInsight;
import com.myname.finguard.crypto.repository.CryptoWalletAnalysisJobRepository;
import com.myname.finguard.crypto.repository.WalletInsightRepository;
import com.myname.finguard.crypto.service.CryptoWalletService;
import com.myname.finguard.dashboard.dto.DashboardOverviewResponse;
import com.myname.finguard.dashboard.dto.UpcomingPaymentDto;
import com.myname.finguard.dashboard.events.UserDataChangedEvent;
import com.myname.finguard.reports.dto.ReportPeriod;
import com.myname.finguard.reports.dto.ReportSummaryResponse;
import com.myname.finguard.reports.service.ReportsService;
import com.myname.finguard.transactions.dto.TransactionDto;
import com.myname.finguard.transactions.service.TransactionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final AccountService accountService;
    private final ReportsService reportsService;
    private final TransactionService transactionService;
    private final CryptoWalletService cryptoWalletService;
    private final CryptoWalletAnalysisJobRepository analysisJobRepository;
    private final WalletInsightRepository walletInsightRepository;
    private final long overviewCacheTtlMs;
    private final Map<Long, CachedOverview> overviewCache = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public DashboardService(
            AccountService accountService,
            ReportsService reportsService,
            TransactionService transactionService,
            CryptoWalletService cryptoWalletService,
            CryptoWalletAnalysisJobRepository analysisJobRepository,
            WalletInsightRepository walletInsightRepository,
            ObjectProvider<MeterRegistry> meterRegistry,
            @Value("${app.dashboard.overview.cache-ttl-ms:10000}") long overviewCacheTtlMs
    ) {
        this.accountService = accountService;
        this.reportsService = reportsService;
        this.transactionService = transactionService;
        this.cryptoWalletService = cryptoWalletService;
        this.analysisJobRepository = analysisJobRepository;
        this.walletInsightRepository = walletInsightRepository;
        this.meterRegistry = meterRegistry.getIfAvailable();
        this.overviewCacheTtlMs = Math.max(0, overviewCacheTtlMs);
    }

    public DashboardOverviewResponse overview(Long userId) {
        long nowMs = System.currentTimeMillis();
        CachedOverview cached = overviewCache.get(userId);
        if (cached != null && overviewCacheTtlMs > 0 && nowMs - cached.cachedAtMs() <= overviewCacheTtlMs) {
            return cached.response();
        }

        Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
        DashboardOverviewResponse response = buildOverview(userId);
        if (sample != null) {
            sample.stop(meterRegistry.timer("dashboard_overview_latency"));
        }
        if (meterRegistry != null) {
            Counter.builder("dashboard_overview_freshness")
                    .tag("state", String.valueOf(response.dataFreshness()).toLowerCase(Locale.ROOT))
                    .register(meterRegistry)
                    .increment();
        }

        overviewCache.put(userId, new CachedOverview(response, nowMs));
        return response;
    }

    public List<UpcomingPaymentDto> upcomingPayments(Long userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        Instant now = Instant.now();
        List<WalletInsight> recurring = walletInsightRepository
                .findTop500ByUserIdAndInsightTypeAndNextEstimatedChargeAtAfterOrderByNextEstimatedChargeAtAsc(
                        userId,
                        "RECURRING_SPEND",
                        now.minus(1, ChronoUnit.DAYS)
                );
        if (recurring.isEmpty()) {
            return List.of();
        }

        List<UpcomingPaymentDto> items = new ArrayList<>();
        for (WalletInsight insight : recurring) {
            if (insight == null || insight.getNextEstimatedChargeAt() == null) {
                continue;
            }
            BigDecimal value = safe(insight.getValue());
            if (value.signum() <= 0) {
                continue;
            }
            String title = firstNonBlank(insight.getLabel(), insight.getTitle(), "Recurring");
            String source = insight.isSynthetic() ? "ESTIMATED" : "LIVE";
            items.add(new UpcomingPaymentDto(
                    "insight-" + insight.getId(),
                    title,
                    value.negate().setScale(2, RoundingMode.HALF_UP),
                    normalizeCurrency(insight.getCurrency()),
                    insight.getNextEstimatedChargeAt(),
                    insight.getConfidence(),
                    source
            ));
            if (items.size() >= safeLimit) {
                break;
            }
        }
        return items;
    }

    public void invalidateUser(Long userId) {
        if (userId == null) {
            return;
        }
        overviewCache.remove(userId);
    }

    @EventListener
    public void onUserDataChanged(UserDataChangedEvent event) {
        if (event == null) {
            return;
        }
        invalidateUser(event.userId());
    }

    private DashboardOverviewResponse buildOverview(Long userId) {
        Instant now = Instant.now();
        UserBalanceResponse balance = accountService.getUserBalance(userId);
        CryptoWalletSummaryResponse walletSummary = cryptoWalletService.walletsSummary(userId);
        ReportSummaryResponse report30d = reportsService.summary(userId, ReportPeriod.MONTH, null, null);
        List<TransactionDto> tx = transactionService.listTransactions(userId, now.minus(30, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), 10);

        String base = normalizeCurrency(firstNonBlank(
                balance == null ? null : balance.baseCurrency(),
                walletSummary == null ? null : walletSummary.baseCurrency(),
                report30d == null ? null : report30d.baseCurrency(),
                "USD"
        ));

        BigDecimal accountsTotal = safe(balance == null ? null : balance.totalInBase());
        BigDecimal walletsTotal = safe(walletSummary == null ? null : walletSummary.totalValueInBase());
        boolean walletsEstimated = walletSummary != null && walletSummary.totalValueInBase() == null;

        BigDecimal netWorth = accountsTotal.add(walletsTotal).setScale(scaleFor(base), RoundingMode.HALF_UP);
        BigDecimal income = safe(report30d == null ? null : report30d.income()).setScale(scaleFor(base), RoundingMode.HALF_UP);
        BigDecimal spend = safe(report30d == null ? null : report30d.expense()).setScale(scaleFor(base), RoundingMode.HALF_UP);
        BigDecimal cashflow = safe(report30d == null ? null : report30d.net()).setScale(scaleFor(base), RoundingMode.HALF_UP);

        BigDecimal debt = sumDebt(balance == null ? null : balance.accounts()).setScale(scaleFor(base), RoundingMode.HALF_UP);
        BigDecimal delta7d = computeDelta7d(userId, netWorth);

        List<DashboardOverviewResponse.TransactionPreview> txPreview = tx.stream()
                .map(item -> new DashboardOverviewResponse.TransactionPreview(
                        item.id(),
                        item.transactionDate(),
                        item.description(),
                        item.type() == null ? "" : item.type().name(),
                        item.amount(),
                        normalizeCurrency(item.currency())
                ))
                .toList();

        List<CryptoWalletDto> wallets = walletSummary == null || walletSummary.wallets() == null
                ? List.of()
                : walletSummary.wallets();

        List<DashboardOverviewResponse.WalletPreview> walletPreview = wallets.stream()
                .limit(5)
                .map(item -> new DashboardOverviewResponse.WalletPreview(
                        item.id(),
                        firstNonBlank(item.label(), item.network(), "Wallet"),
                        item.network(),
                        item.address(),
                        item.valueInBase(),
                        normalizeCurrency(firstNonBlank(item.baseCurrency(), base))
                ))
                .toList();

        List<UpcomingPaymentDto> upcoming = upcomingPayments(userId, 5);

        Long activeWalletId = wallets.isEmpty() ? null : wallets.get(0).id();
        CryptoWalletAnalysisJob latestJob = null;
        if (activeWalletId != null) {
            latestJob = analysisJobRepository.findTopByWalletIdAndUserIdOrderByCreatedAtDesc(activeWalletId, userId).orElse(null);
        }

        String freshness = resolveFreshness(walletsEstimated, latestJob, txPreview, walletPreview, upcoming);
        boolean getStartedVisible = walletPreview.isEmpty() || txPreview.isEmpty() || (balance != null && (balance.accounts() == null || balance.accounts().isEmpty()));

        DashboardOverviewResponse.Hero hero = new DashboardOverviewResponse.Hero(
                netWorth,
                base,
                delta7d,
                now,
                netWorth.signum() != 0 || !txPreview.isEmpty() || !walletPreview.isEmpty()
        );

        DashboardOverviewResponse.Stats stats = new DashboardOverviewResponse.Stats(
                income,
                spend,
                cashflow,
                debt,
                income.signum() != 0 || spend.signum() != 0 || cashflow.signum() != 0 || debt.signum() != 0
        );

        DashboardOverviewResponse.GetStarted getStarted = new DashboardOverviewResponse.GetStarted(
                getStartedVisible,
                balance == null || balance.accounts() == null || balance.accounts().isEmpty(),
                txPreview.isEmpty(),
                upcoming.isEmpty()
        );

        DashboardOverviewResponse.WalletIntelligence walletIntelligence = new DashboardOverviewResponse.WalletIntelligence(
                activeWalletId,
                latestJob == null || latestJob.getStatus() == null ? "QUEUED" : latestJob.getStatus().name(),
                latestJob == null ? 0 : latestJob.getProgressPct(),
                latestJob != null && (latestJob.getStatus() == CryptoWalletAnalysisJobStatus.PARTIAL || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.DONE),
                latestJob == null ? now : latestJob.getUpdatedAt(),
                latestJob == null || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.DONE ? "LIVE" : "PARTIAL"
        );

        return new DashboardOverviewResponse(
                now,
                freshness,
                hero,
                stats,
                getStarted,
                txPreview,
                walletPreview,
                upcoming,
                walletIntelligence
        );
    }

    private String resolveFreshness(
            boolean walletsEstimated,
            CryptoWalletAnalysisJob latestJob,
            List<DashboardOverviewResponse.TransactionPreview> tx,
            List<DashboardOverviewResponse.WalletPreview> wallets,
            List<UpcomingPaymentDto> upcoming
    ) {
        if (latestJob != null && latestJob.getStatus() != null) {
            if (latestJob.getStatus() == CryptoWalletAnalysisJobStatus.RUNNING
                    || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.QUEUED
                    || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.PARTIAL) {
                return "PARTIAL";
            }
        }
        if (walletsEstimated) {
            return "ESTIMATED";
        }
        if (!tx.isEmpty() || !wallets.isEmpty() || !upcoming.isEmpty()) {
            return "LIVE";
        }
        return "ESTIMATED";
    }

    private BigDecimal computeDelta7d(Long userId, BigDecimal netWorth) {
        ReportSummaryResponse report7d = reportsService.summary(userId, ReportPeriod.WEEK, null, null);
        BigDecimal weekNet = safe(report7d == null ? null : report7d.net());
        BigDecimal denominator = netWorth == null || netWorth.signum() == 0
                ? BigDecimal.ONE
                : netWorth.abs();
        return weekNet.multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumDebt(List<UserBalanceResponse.AccountBalance> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (UserBalanceResponse.AccountBalance account : accounts) {
            if (account == null || account.archived()) {
                continue;
            }
            BigDecimal balance = safe(account.balance());
            if (balance.signum() < 0) {
                sum = sum.add(balance.abs());
            }
        }
        return sum;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int scaleFor(String currency) {
        String normalized = normalizeCurrency(currency);
        if ("BTC".equals(normalized) || "ETH".equals(normalized)) {
            return 8;
        }
        return 2;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "USD";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private record CachedOverview(DashboardOverviewResponse response, long cachedAtMs) {
        CachedOverview {
            Objects.requireNonNull(response, "response");
        }
    }
}
