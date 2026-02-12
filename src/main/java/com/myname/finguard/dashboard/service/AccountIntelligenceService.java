package com.myname.finguard.dashboard.service;

import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.service.AccountService;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.dto.CryptoWalletSummaryResponse;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJobStatus;
import com.myname.finguard.crypto.model.WalletInsight;
import com.myname.finguard.crypto.repository.CryptoWalletAnalysisJobRepository;
import com.myname.finguard.crypto.repository.WalletInsightRepository;
import com.myname.finguard.crypto.service.CryptoWalletService;
import com.myname.finguard.dashboard.dto.AccountInsightDto;
import com.myname.finguard.dashboard.dto.AccountIntelligenceResponse;
import com.myname.finguard.dashboard.events.UserDataChangedEvent;
import com.myname.finguard.reports.dto.CashFlowResponse;
import com.myname.finguard.reports.dto.ReportPeriod;
import com.myname.finguard.reports.dto.ReportSummaryResponse;
import com.myname.finguard.reports.dto.ReportsByCategoryResponse;
import com.myname.finguard.reports.service.ReportsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountIntelligenceService {

    private static final BigDecimal EPS = new BigDecimal("0.000001");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final double COMPACT_VARIANCE_THRESHOLD = 0.015d;

    private final AccountService accountService;
    private final ReportsService reportsService;
    private final CryptoWalletService cryptoWalletService;
    private final WalletInsightRepository walletInsightRepository;
    private final CryptoWalletAnalysisJobRepository analysisJobRepository;
    private final MeterRegistry meterRegistry;
    private final long cacheTtlMs;
    private final Map<CacheKey, CachedPayload> cache = new ConcurrentHashMap<>();

    public AccountIntelligenceService(
            AccountService accountService,
            ReportsService reportsService,
            CryptoWalletService cryptoWalletService,
            WalletInsightRepository walletInsightRepository,
            CryptoWalletAnalysisJobRepository analysisJobRepository,
            ObjectProvider<MeterRegistry> meterRegistry,
            @Value("${app.dashboard.account-intelligence.cache-ttl-ms:10000}") long cacheTtlMs
    ) {
        this.accountService = accountService;
        this.reportsService = reportsService;
        this.cryptoWalletService = cryptoWalletService;
        this.walletInsightRepository = walletInsightRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.meterRegistry = meterRegistry.getIfAvailable();
        this.cacheTtlMs = Math.max(0, cacheTtlMs);
    }

    public AccountIntelligenceResponse accountIntelligence(Long userId, String window, String metric) {
        String normalizedWindow = normalizeWindow(window);
        String normalizedMetric = normalizeMetric(metric);
        CacheKey key = new CacheKey(userId, normalizedWindow, normalizedMetric);
        long nowMs = System.currentTimeMillis();

        CachedPayload cached = cache.get(key);
        if (cached != null && cacheTtlMs > 0 && nowMs - cached.cachedAtMs() <= cacheTtlMs) {
            recordCacheMetric("hit");
            return cached.response();
        }
        recordCacheMetric("miss");

        Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
        try {
            AccountIntelligenceResponse response = build(userId, normalizedWindow, normalizedMetric);
            cache.put(key, new CachedPayload(response, nowMs));
            if (sample != null) {
                sample.stop(meterRegistry.timer("account_intelligence_latency"));
            }
            if (meterRegistry != null) {
                Counter.builder("account_intelligence_source_ratio")
                        .tag("source", firstNonBlank(response.source(), "unknown").toLowerCase(Locale.ROOT))
                        .register(meterRegistry)
                        .increment();
            }
            return response;
        } catch (RuntimeException ex) {
            if (meterRegistry != null) {
                Counter.builder("dashboard_error_rate")
                        .tag("endpoint", "account-intelligence")
                        .register(meterRegistry)
                        .increment();
            }
            throw ex;
        }
    }

    public void invalidateUser(Long userId) {
        if (userId == null) {
            return;
        }
        cache.keySet().removeIf(key -> Objects.equals(key.userId(), userId));
    }

    @EventListener
    public void onUserDataChanged(UserDataChangedEvent event) {
        if (event == null) {
            return;
        }
        invalidateUser(event.userId());
    }

    private AccountIntelligenceResponse build(Long userId, String window, String metric) {
        Instant now = Instant.now();
        int windowDays = parseWindowDays(window);
        LocalDate endDay = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate startDay = endDay.minusDays(windowDays - 1L);
        Instant rangeFrom = startDay.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant rangeTo = endDay.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        UserBalanceResponse balance = accountService.getUserBalance(userId);
        CryptoWalletSummaryResponse walletSummary = cryptoWalletService.walletsSummary(userId);
        ReportSummaryResponse monthSummary = reportsService.summary(userId, ReportPeriod.MONTH, null, null);
        ReportSummaryResponse weekSummary = reportsService.summary(userId, ReportPeriod.WEEK, null, null);
        CashFlowResponse cashFlow = reportsService.cashFlow(userId, rangeFrom, rangeTo);
        ReportsByCategoryResponse byCategory = reportsService.byCategory(userId, ReportPeriod.MONTH, null, null, 1);

        List<CryptoWalletDto> wallets = walletSummary == null || walletSummary.wallets() == null
                ? List.of()
                : walletSummary.wallets();
        List<UserBalanceResponse.AccountBalance> accounts = balance == null || balance.accounts() == null
                ? List.of()
                : balance.accounts();

        String base = normalizeCurrency(firstNonBlank(
                balance == null ? null : balance.baseCurrency(),
                walletSummary == null ? null : walletSummary.baseCurrency(),
                monthSummary == null ? null : monthSummary.baseCurrency(),
                cashFlow == null ? null : cashFlow.baseCurrency(),
                "USD"
        ));
        int scale = scaleFor(base);

        BigDecimal accountsTotal = safe(balance == null ? null : balance.totalInBase());
        BigDecimal walletsTotalRaw = walletSummary == null ? null : walletSummary.totalValueInBase();
        boolean walletsEstimated = !wallets.isEmpty() && walletsTotalRaw == null;
        BigDecimal walletsTotal = safe(walletsTotalRaw);
        BigDecimal netWorth = scaleAmount(accountsTotal.add(walletsTotal), scale);

        BigDecimal income30d = scaleAmount(safe(monthSummary == null ? null : monthSummary.income()), scale);
        BigDecimal spend30d = scaleAmount(safe(monthSummary == null ? null : monthSummary.expense()), scale);
        BigDecimal cashflow30d = scaleAmount(safe(monthSummary == null ? null : monthSummary.net()), scale);
        BigDecimal debt = scaleAmount(sumDebt(accounts), scale);
        BigDecimal delta7dPct = computeDelta7d(safe(weekSummary == null ? null : weekSummary.net()), netWorth);

        List<AccountIntelligenceResponse.SeriesPoint> seriesPoints = buildSeriesPoints(
                cashFlow == null ? null : cashFlow.points(),
                startDay,
                endDay,
                metric,
                netWorth,
                scale
        );
        boolean seriesMeaningful = hasMeaningfulSeries(seriesPoints);
        boolean seriesCompact = isCompactSeries(seriesPoints);

        List<AccountIntelligenceResponse.WalletAllocationItem> allocation = buildWalletAllocation(wallets, base);
        List<AccountInsightDto> insights = buildInsights(
                userId,
                base,
                scale,
                income30d,
                spend30d,
                cashflow30d,
                byCategory
        );

        CryptoWalletAnalysisJob latestJob = analysisJobRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        boolean summaryMeaningful = hasMeaningfulAmount(netWorth)
                || hasMeaningfulAmount(income30d)
                || hasMeaningfulAmount(spend30d)
                || hasMeaningfulAmount(cashflow30d)
                || hasMeaningfulAmount(debt)
                || !accounts.isEmpty()
                || !wallets.isEmpty();
        String source = resolveSource(walletsEstimated, latestJob, summaryMeaningful, seriesMeaningful, insights);
        AccountIntelligenceResponse.Status status = buildStatus(latestJob, source, now);

        boolean hasMeaningfulData = summaryMeaningful
                || seriesMeaningful
                || allocation.stream().anyMatch(AccountIntelligenceResponse.WalletAllocationItem::hasMeaningfulData)
                || !insights.isEmpty();

        return new AccountIntelligenceResponse(
                now,
                source,
                new AccountIntelligenceResponse.Summary(
                        netWorth,
                        base,
                        delta7dPct,
                        income30d,
                        spend30d,
                        cashflow30d,
                        debt,
                        summaryMeaningful
                ),
                new AccountIntelligenceResponse.Series(
                        window,
                        metric,
                        seriesPoints,
                        seriesCompact,
                        seriesMeaningful
                ),
                allocation,
                insights,
                status,
                hasMeaningfulData
        );
    }

    private List<AccountIntelligenceResponse.SeriesPoint> buildSeriesPoints(
            List<CashFlowResponse.CashFlowPoint> points,
            LocalDate startDay,
            LocalDate endDay,
            String metric,
            BigDecimal netWorth,
            int scale
    ) {
        if (startDay == null || endDay == null || endDay.isBefore(startDay)) {
            return List.of();
        }
        Map<LocalDate, CashFlowResponse.CashFlowPoint> byDay = new HashMap<>();
        if (points != null) {
            for (CashFlowResponse.CashFlowPoint point : points) {
                if (point == null || point.date() == null) {
                    continue;
                }
                byDay.put(point.date(), point);
            }
        }

        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = startDay;
        while (!cursor.isAfter(endDay)) {
            days.add(cursor);
            cursor = cursor.plusDays(1);
        }

        List<BigDecimal> inflow = new ArrayList<>(days.size());
        List<BigDecimal> outflow = new ArrayList<>(days.size());
        List<BigDecimal> net = new ArrayList<>(days.size());
        for (LocalDate day : days) {
            CashFlowResponse.CashFlowPoint point = byDay.get(day);
            BigDecimal in = safe(point == null ? null : point.income());
            BigDecimal out = safe(point == null ? null : point.expense());
            BigDecimal dailyNet = safe(point == null ? null : point.net());
            inflow.add(scaleAmount(in.abs(), scale));
            outflow.add(scaleAmount(out.abs(), scale));
            net.add(scaleAmount(dailyNet, scale));
        }

        List<BigDecimal> values;
        if ("inflow".equals(metric)) {
            values = inflow;
        } else if ("outflow".equals(metric)) {
            values = outflow;
        } else {
            BigDecimal totalDelta = net.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal baseline = safe(netWorth).subtract(totalDelta);
            BigDecimal running = baseline;
            values = new ArrayList<>(net.size());
            for (BigDecimal delta : net) {
                running = running.add(safe(delta));
                values.add(scaleAmount(running, scale));
            }
        }

        List<AccountIntelligenceResponse.SeriesPoint> series = new ArrayList<>(days.size());
        for (int i = 0; i < days.size(); i += 1) {
            series.add(new AccountIntelligenceResponse.SeriesPoint(
                    days.get(i).atStartOfDay().toInstant(ZoneOffset.UTC),
                    scaleAmount(values.get(i), scale)
            ));
        }
        return series;
    }

    private List<AccountIntelligenceResponse.WalletAllocationItem> buildWalletAllocation(
            List<CryptoWalletDto> wallets,
            String baseCurrency
    ) {
        if (wallets == null || wallets.isEmpty()) {
            return List.of();
        }
        BigDecimal total = wallets.stream()
                .map(CryptoWalletDto::valueInBase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int scale = scaleFor(baseCurrency);
        List<AccountIntelligenceResponse.WalletAllocationItem> rows = new ArrayList<>();
        for (CryptoWalletDto item : wallets) {
            if (item == null) {
                continue;
            }
            BigDecimal valueInBase = item.valueInBase() == null ? null : scaleAmount(item.valueInBase(), scale);
            BigDecimal sharePct = null;
            if (valueInBase != null && total.signum() > 0) {
                sharePct = valueInBase.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
            }
            BigDecimal amount = item.balance() == null ? null : scaleAmount(item.balance(), 8);
            rows.add(new AccountIntelligenceResponse.WalletAllocationItem(
                    item.id(),
                    firstNonBlank(item.label(), item.network(), "Wallet"),
                    firstNonBlank(item.network(), ""),
                    firstNonBlank(item.address(), ""),
                    amount,
                    walletAsset(item.network()),
                    valueInBase,
                    sharePct,
                    hasMeaningfulAmount(valueInBase) || hasMeaningfulAmount(amount)
            ));
        }
        rows.sort(Comparator.comparing(AccountIntelligenceResponse.WalletAllocationItem::valueInBase, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
        return rows;
    }

    private List<AccountInsightDto> buildInsights(
            Long userId,
            String baseCurrency,
            int scale,
            BigDecimal income30d,
            BigDecimal spend30d,
            BigDecimal cashflow30d,
            ReportsByCategoryResponse byCategory
    ) {
        List<AccountInsightDto> insights = new ArrayList<>();

        List<WalletInsight> walletInsights = walletInsightRepository.findTop500ByUserIdOrderByAsOfDescIdDesc(userId);
        Set<String> usedTypes = new HashSet<>();
        for (WalletInsight item : walletInsights) {
            if (item == null || item.isSynthetic()) {
                continue;
            }
            String type = firstNonBlank(item.getInsightType(), "INSIGHT");
            if (!usedTypes.add(type)) {
                continue;
            }
            BigDecimal value = item.getValue();
            if (!hasMeaningfulAmount(value)) {
                continue;
            }
            String unit = normalizeInsightUnit(item.getUnit(), item.getCurrency(), baseCurrency);
            String meta = buildInsightMeta(item);
            BigDecimal confidence = item.getConfidence() == null ? null : item.getConfidence().setScale(4, RoundingMode.HALF_UP);
            boolean actionable = "RECURRING_SPEND".equalsIgnoreCase(type)
                    || "TOP_OUTFLOW".equalsIgnoreCase(type)
                    || "ANOMALOUS_OUTFLOWS".equalsIgnoreCase(type);
            insights.add(new AccountInsightDto(
                    type,
                    firstNonBlank(item.getTitle(), item.getLabel(), type),
                    scaleInsightValue(value, unit, scale),
                    unit,
                    meta,
                    confidence,
                    actionable
            ));
            if (insights.size() >= 4) {
                break;
            }
        }

        if (hasMeaningfulAmount(income30d)) {
            insights.add(new AccountInsightDto("INCOME_30D", "Income (30d)", income30d, "BASE_CURRENCY:" + baseCurrency, "30d", BigDecimal.ONE, false));
        }
        if (hasMeaningfulAmount(spend30d)) {
            insights.add(new AccountInsightDto("SPEND_30D", "Spend (30d)", spend30d.negate(), "BASE_CURRENCY:" + baseCurrency, "30d", BigDecimal.ONE, true));
        }
        if (hasMeaningfulAmount(cashflow30d)) {
            insights.add(new AccountInsightDto("CASHFLOW_30D", "Cashflow (30d)", cashflow30d, "BASE_CURRENCY:" + baseCurrency, "30d", BigDecimal.ONE, false));
        }

        if (byCategory != null && byCategory.expenses() != null && !byCategory.expenses().isEmpty()) {
            ReportsByCategoryResponse.CategoryTotal top = byCategory.expenses().get(0);
            BigDecimal value = top == null ? null : top.total();
            if (hasMeaningfulAmount(value)) {
                insights.add(new AccountInsightDto(
                        "TOP_SPEND_CATEGORY",
                        "Top spend category",
                        scaleAmount(value.negate(), scale),
                        "BASE_CURRENCY:" + baseCurrency,
                        firstNonBlank(top.categoryName(), "Category"),
                        BigDecimal.ONE,
                        true
                ));
            }
        }

        return insights.stream()
                .filter(item -> item != null && hasMeaningfulAmount(item.value()))
                .limit(8)
                .toList();
    }

    private AccountIntelligenceResponse.Status buildStatus(CryptoWalletAnalysisJob latestJob, String source, Instant now) {
        if (latestJob == null) {
            return new AccountIntelligenceResponse.Status(
                    "PENDING",
                    0,
                    false,
                    now,
                    source,
                    null,
                    null
            );
        }
        String status = latestJob.getStatus() == null ? "QUEUED" : latestJob.getStatus().name();
        boolean partialReady = latestJob.getStatus() == CryptoWalletAnalysisJobStatus.PARTIAL
                || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.DONE;
        return new AccountIntelligenceResponse.Status(
                status,
                Math.max(0, Math.min(100, latestJob.getProgressPct())),
                partialReady,
                firstNonNull(latestJob.getUpdatedAt(), now),
                source,
                estimateEtaSeconds(latestJob),
                latestJob.getLastSuccessfulStage() == null ? null : latestJob.getLastSuccessfulStage().name()
        );
    }

    private Integer estimateEtaSeconds(CryptoWalletAnalysisJob job) {
        if (job == null || job.getStartedAt() == null) {
            return null;
        }
        int progress = Math.max(0, Math.min(job.getProgressPct(), 100));
        if (progress >= 100) {
            return 0;
        }
        if (progress == 0) {
            return null;
        }
        long elapsedMs = Math.max(1, Duration.between(job.getStartedAt(), Instant.now()).toMillis());
        long estimatedTotalMs = Math.max(elapsedMs, Math.round(elapsedMs * (100.0 / progress)));
        long etaMs = Math.max(0, estimatedTotalMs - elapsedMs);
        return (int) Math.min(Integer.MAX_VALUE, Math.ceil(etaMs / 1000.0));
    }

    private String resolveSource(
            boolean walletsEstimated,
            CryptoWalletAnalysisJob latestJob,
            boolean summaryMeaningful,
            boolean seriesMeaningful,
            List<AccountInsightDto> insights
    ) {
        if (!summaryMeaningful && !seriesMeaningful && (insights == null || insights.isEmpty())) {
            return "PENDING";
        }
        if (walletsEstimated) {
            return "ESTIMATED";
        }
        if (latestJob != null && latestJob.getStatus() != null) {
            if (latestJob.getStatus() == CryptoWalletAnalysisJobStatus.RUNNING
                    || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.QUEUED
                    || latestJob.getStatus() == CryptoWalletAnalysisJobStatus.PARTIAL) {
                return "PARTIAL";
            }
        }
        return "LIVE";
    }

    private String normalizeWindow(String value) {
        String normalized = value == null ? "30d" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "7d" -> "7d";
            case "30d", "" -> "30d";
            case "90d" -> "90d";
            case "1y", "365d" -> "1y";
            default -> throw new ApiException(
                    ErrorCodes.BAD_REQUEST,
                    "Unsupported window. Use one of: 7d, 30d, 90d, 1y",
                    HttpStatus.BAD_REQUEST
            );
        };
    }

    private int parseWindowDays(String window) {
        return switch (normalizeWindow(window)) {
            case "7d" -> 7;
            case "90d" -> 90;
            case "1y" -> 365;
            default -> 30;
        };
    }

    private String normalizeMetric(String value) {
        String normalized = value == null ? "net" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "net" -> "net";
            case "inflow" -> "inflow";
            case "outflow" -> "outflow";
            default -> throw new ApiException(
                    ErrorCodes.BAD_REQUEST,
                    "Unsupported metric. Use one of: net, inflow, outflow",
                    HttpStatus.BAD_REQUEST
            );
        };
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "USD";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String walletAsset(String network) {
        String normalized = firstNonBlank(network, "").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "BTC" -> "BTC";
            case "ETH", "ARBITRUM", "EVM" -> "ETH";
            default -> normalized.isBlank() ? "ASSET" : normalized;
        };
    }

    private BigDecimal computeDelta7d(BigDecimal weekNet, BigDecimal netWorth) {
        BigDecimal denominator = safe(netWorth);
        if (denominator.signum() == 0) {
            denominator = BigDecimal.ONE;
        }
        return safe(weekNet)
                .multiply(HUNDRED)
                .divide(denominator.abs(), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumDebt(Collection<UserBalanceResponse.AccountBalance> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal debt = BigDecimal.ZERO;
        for (UserBalanceResponse.AccountBalance account : accounts) {
            if (account == null || account.archived()) {
                continue;
            }
            BigDecimal balance = safe(account.balance());
            if (balance.signum() < 0) {
                debt = debt.add(balance.abs());
            }
        }
        return debt;
    }

    private boolean hasMeaningfulSeries(List<AccountIntelligenceResponse.SeriesPoint> points) {
        if (points == null || points.isEmpty()) {
            return false;
        }
        for (AccountIntelligenceResponse.SeriesPoint point : points) {
            if (point != null && hasMeaningfulAmount(point.valueInBase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCompactSeries(List<AccountIntelligenceResponse.SeriesPoint> points) {
        if (points == null || points.size() < 4) {
            return true;
        }
        List<BigDecimal> values = points.stream()
                .map(AccountIntelligenceResponse.SeriesPoint::valueInBase)
                .filter(Objects::nonNull)
                .toList();
        if (values.size() < 4) {
            return true;
        }
        BigDecimal min = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal span = max.subtract(min).abs();
        BigDecimal avg = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 8, RoundingMode.HALF_UP)
                .abs();
        BigDecimal baseline = avg.max(values.get(0).abs()).max(values.get(values.size() - 1).abs()).max(BigDecimal.ONE);
        double ratio = span.divide(baseline, 8, RoundingMode.HALF_UP).doubleValue();
        return ratio < COMPACT_VARIANCE_THRESHOLD;
    }

    private String normalizeInsightUnit(String unit, String currency, String baseCurrency) {
        String normalized = firstNonBlank(unit, "").toUpperCase(Locale.ROOT);
        if ("PERCENT".equals(normalized) || "COUNT".equals(normalized)) {
            return normalized;
        }
        String currencyCode = normalizeCurrency(firstNonBlank(currency, baseCurrency));
        return "BASE_CURRENCY:" + currencyCode;
    }

    private String buildInsightMeta(WalletInsight item) {
        if (item == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (item.getLabel() != null && !item.getLabel().isBlank()) {
            parts.add(item.getLabel().trim());
        }
        if (item.getNextEstimatedChargeAt() != null) {
            parts.add("next " + item.getNextEstimatedChargeAt().toString());
        }
        if (item.getSource() != null && !item.getSource().isBlank()) {
            parts.add(item.getSource().trim().toUpperCase(Locale.ROOT));
        }
        return String.join(" · ", parts);
    }

    private BigDecimal scaleInsightValue(BigDecimal value, String unit, int amountScale) {
        if (value == null) {
            return null;
        }
        if ("PERCENT".equalsIgnoreCase(unit)) {
            return value.setScale(2, RoundingMode.HALF_UP);
        }
        if ("COUNT".equalsIgnoreCase(unit)) {
            return value.setScale(0, RoundingMode.HALF_UP);
        }
        return value.setScale(amountScale, RoundingMode.HALF_UP);
    }

    private boolean hasMeaningfulAmount(BigDecimal value) {
        return value != null && value.abs().compareTo(EPS) > 0;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scaleAmount(BigDecimal value, int scale) {
        return safe(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private int scaleFor(String currency) {
        String normalized = normalizeCurrency(currency);
        if ("BTC".equals(normalized) || "ETH".equals(normalized)) {
            return 8;
        }
        return 2;
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

    private Instant firstNonNull(Instant value, Instant fallback) {
        return value == null ? fallback : value;
    }

    private void recordCacheMetric(String result) {
        if (meterRegistry == null) {
            return;
        }
        Counter.builder("account_intelligence_cache")
                .tag("result", firstNonBlank(result, "unknown").toLowerCase(Locale.ROOT))
                .register(meterRegistry)
                .increment();
    }

    private record CacheKey(Long userId, String window, String metric) {
    }

    private record CachedPayload(AccountIntelligenceResponse response, long cachedAtMs) {
        CachedPayload {
            Objects.requireNonNull(response, "response");
        }
    }
}
