package com.myname.finguard.crypto.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.CryptoRatesService;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisInsightItem;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisInsightsResponse;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisStatusResponse;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisSummaryResponse;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.dto.CryptoWalletSummaryResponse;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJobStatus;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisStage;
import com.myname.finguard.crypto.repository.CryptoWalletAnalysisJobRepository;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import com.myname.finguard.reports.dto.ReportPeriod;
import com.myname.finguard.reports.dto.ReportSummaryResponse;
import com.myname.finguard.reports.dto.ReportsByCategoryResponse;
import com.myname.finguard.reports.service.ReportsService;
import com.myname.finguard.transactions.model.Transaction;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CryptoWalletAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CryptoWalletAnalysisService.class);

    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoWalletAnalysisJobRepository jobRepository;
    private final CryptoWalletService cryptoWalletService;
    private final ReportsService reportsService;
    private final TransactionRepository transactionRepository;
    private final CurrencyService currencyService;
    private final CryptoRatesService cryptoRatesService;
    private final long simulatedDelayMs;

    public CryptoWalletAnalysisService(
            CryptoWalletRepository cryptoWalletRepository,
            CryptoWalletAnalysisJobRepository jobRepository,
            @Lazy CryptoWalletService cryptoWalletService,
            ReportsService reportsService,
            TransactionRepository transactionRepository,
            CurrencyService currencyService,
            CryptoRatesService cryptoRatesService,
            @Value("${app.crypto.analysis.simulated-delay-ms:300}") long simulatedDelayMs
    ) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.jobRepository = jobRepository;
        this.cryptoWalletService = cryptoWalletService;
        this.reportsService = reportsService;
        this.transactionRepository = transactionRepository;
        this.currencyService = currencyService;
        this.cryptoRatesService = cryptoRatesService;
        this.simulatedDelayMs = Math.max(0, simulatedDelayMs);
    }

    public void enqueueInitialAnalysis(CryptoWallet wallet) {
        if (wallet == null || wallet.getId() == null || wallet.getUser() == null || wallet.getUser().getId() == null) {
            return;
        }
        CryptoWalletAnalysisJob existing = latestJob(wallet.getUser().getId(), wallet.getId());
        if (existing != null && !isTerminal(existing.getStatus())) {
            return;
        }
        CryptoWalletAnalysisJob job = new CryptoWalletAnalysisJob();
        job.setUser(wallet.getUser());
        job.setWallet(wallet);
        job.setStatus(CryptoWalletAnalysisJobStatus.QUEUED);
        job.setStage(CryptoWalletAnalysisStage.FETCH_TX);
        job.setProgressPct(0);
        job.setErrorMessage(null);
        CryptoWalletAnalysisJob saved = jobRepository.save(job);
        runAsync(saved.getId());
    }

    public CryptoWalletAnalysisStatusResponse status(Long userId, Long walletId) {
        if (userId == null || walletId == null) {
            throw walletNotFound();
        }
        CryptoWallet wallet = cryptoWalletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(this::walletNotFound);

        CryptoWalletAnalysisJob latest = latestJob(userId, walletId);
        if (latest == null) {
            enqueueInitialAnalysis(wallet);
            latest = latestJob(userId, walletId);
        }
        if (latest == null) {
            return new CryptoWalletAnalysisStatusResponse(
                    CryptoWalletAnalysisJobStatus.QUEUED.name(),
                    0,
                    CryptoWalletAnalysisStage.FETCH_TX.name(),
                    null,
                    Instant.now(),
                    null,
                    false
            );
        }

        return toResponse(latest);
    }

    public CryptoWalletAnalysisSummaryResponse summary(Long userId, Long walletId) {
        requireWallet(userId, walletId);
        Instant now = Instant.now();
        CryptoWalletSummaryResponse summary = cryptoWalletService.walletsSummary(userId);

        String baseCurrency = normalizeCurrency(summary == null ? null : summary.baseCurrency());
        BigDecimal total = safe(summary == null ? null : summary.totalValueInBase());
        boolean synthetic = summary == null || summary.totalValueInBase() == null;

        BigDecimal delta24hPct = computeDeltaPct(userId, now.minus(24, ChronoUnit.HOURS), now, total);
        BigDecimal delta7dPct = computeDeltaPct(userId, now.minus(7, ChronoUnit.DAYS), now, total);

        List<CryptoWalletAnalysisSummaryResponse.AllocationItem> allocation = buildAllocation(summary);
        return new CryptoWalletAnalysisSummaryResponse(
                total,
                baseCurrency,
                delta24hPct,
                delta7dPct,
                allocation,
                now,
                synthetic
        );
    }

    public CryptoWalletAnalysisInsightsResponse insights(Long userId, Long walletId) {
        requireWallet(userId, walletId);
        Instant now = Instant.now();
        CryptoWalletSummaryResponse walletSummary = cryptoWalletService.walletsSummary(userId);
        String baseCurrency = normalizeCurrency(walletSummary == null ? null : walletSummary.baseCurrency());
        BigDecimal total = safe(walletSummary == null ? null : walletSummary.totalValueInBase());

        List<CryptoWalletAnalysisInsightItem> insights = new ArrayList<>();

        ReportsByCategoryResponse byCategory = reportsService.byCategory(userId, ReportPeriod.MONTH, null, null, 1);
        if (byCategory != null && byCategory.expenses() != null && !byCategory.expenses().isEmpty()) {
            ReportsByCategoryResponse.CategoryTotal top = byCategory.expenses().get(0);
            insights.add(new CryptoWalletAnalysisInsightItem(
                    "TOP_OUTFLOW",
                    "Top outflow",
                    safe(top.total()),
                    "BASE_CURRENCY",
                    baseCurrency,
                    top.categoryName(),
                    BigDecimal.valueOf(0.92),
                    now,
                    false
            ));
        } else {
            insights.add(new CryptoWalletAnalysisInsightItem(
                    "TOP_OUTFLOW",
                    "Top outflow",
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    "BASE_CURRENCY",
                    baseCurrency,
                    null,
                    BigDecimal.valueOf(0.35),
                    now,
                    true
            ));
        }

        RecurringCandidate recurring = detectRecurring(userId, baseCurrency, now);
        insights.add(new CryptoWalletAnalysisInsightItem(
                "RECURRING_SPEND",
                "Recurring spend",
                recurring.amount(),
                "BASE_CURRENCY",
                baseCurrency,
                recurring.label(),
                recurring.confidence(),
                now,
                recurring.synthetic()
        ));

        ReportSummaryResponse current = reportsService.summary(userId, ReportPeriod.MONTH, null, null);
        ReportSummaryResponse previous = reportsService.summary(
                userId,
                null,
                now.minus(60, ChronoUnit.DAYS),
                now.minus(30, ChronoUnit.DAYS)
        );
        BigDecimal currentNet = safe(current == null ? null : current.net());
        BigDecimal previousNet = safe(previous == null ? null : previous.net());
        BigDecimal deltaNet = currentNet.subtract(previousNet);
        BigDecimal trendDenominator = total.signum() > 0 ? total : absOrOne(previousNet);
        BigDecimal trendPct = trendDenominator.signum() == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : deltaNet.multiply(BigDecimal.valueOf(100)).divide(trendDenominator, 2, RoundingMode.HALF_UP);
        insights.add(new CryptoWalletAnalysisInsightItem(
                "PORTFOLIO_30D_CHANGE",
                "30d portfolio trend",
                trendPct,
                "PERCENT",
                null,
                null,
                BigDecimal.valueOf(0.72),
                now,
                false
        ));

        AnomalyResult anomaly = detectAnomalies(userId, baseCurrency, now);
        insights.add(new CryptoWalletAnalysisInsightItem(
                "ANOMALOUS_OUTFLOWS",
                "Anomalous outflows",
                anomaly.count(),
                "COUNT",
                null,
                null,
                anomaly.confidence(),
                now,
                anomaly.synthetic()
        ));

        return new CryptoWalletAnalysisInsightsResponse(baseCurrency, insights, now);
    }

    protected CryptoWalletAnalysisJob latestJob(Long userId, Long walletId) {
        return jobRepository.findTopByWalletIdAndUserIdOrderByCreatedAtDesc(walletId, userId).orElse(null);
    }

    private void runAsync(Long jobId) {
        if (jobId == null) {
            return;
        }
        CompletableFuture.runAsync(() -> runPipeline(jobId))
                .exceptionally(ex -> {
                    log.debug("Wallet analysis async task failed for jobId={}: {}", jobId, ex.getMessage());
                    return null;
                });
    }

    protected void runPipeline(Long jobId) {
        try {
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.FETCH_TX, 10, null, true, false);
            sleepStep();
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.ENRICH_TX, 35, null, false, false);
            sleepStep();
            updateJob(jobId, CryptoWalletAnalysisJobStatus.PARTIAL, CryptoWalletAnalysisStage.BUILD_SNAPSHOTS, 58, null, false, false);
            sleepStep();
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.DETECT_RECURRING, 78, null, false, false);
            sleepStep();
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.BUILD_INSIGHTS, 92, null, false, false);
            sleepStep();
            updateJob(jobId, CryptoWalletAnalysisJobStatus.DONE, CryptoWalletAnalysisStage.DONE, 100, null, false, true);
        } catch (Exception ex) {
            updateJob(jobId, CryptoWalletAnalysisJobStatus.FAILED, CryptoWalletAnalysisStage.BUILD_INSIGHTS, 100, "Analysis failed", false, true);
            log.debug("Wallet analysis pipeline failed for jobId={}: {}", jobId, ex.getMessage());
        }
    }

    protected void updateJob(
            Long jobId,
            CryptoWalletAnalysisJobStatus status,
            CryptoWalletAnalysisStage stage,
            int progressPct,
            String errorMessage,
            boolean markStarted,
            boolean markFinished
    ) {
        if (jobId == null) {
            return;
        }
        CryptoWalletAnalysisJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }
        if (status != null) {
            job.setStatus(status);
        }
        if (stage != null) {
            job.setStage(stage);
        }
        job.setProgressPct(Math.max(0, Math.min(progressPct, 100)));
        job.setErrorMessage(errorMessage);
        if (markStarted && job.getStartedAt() == null) {
            job.setStartedAt(Instant.now());
        }
        if (markFinished) {
            job.setFinishedAt(Instant.now());
        }
        jobRepository.save(job);
    }

    private void sleepStep() {
        if (simulatedDelayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(simulatedDelayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private CryptoWalletAnalysisStatusResponse toResponse(CryptoWalletAnalysisJob job) {
        boolean partialReady = job.getStatus() == CryptoWalletAnalysisJobStatus.PARTIAL
                || job.getStatus() == CryptoWalletAnalysisJobStatus.DONE;
        return new CryptoWalletAnalysisStatusResponse(
                job.getStatus() == null ? CryptoWalletAnalysisJobStatus.QUEUED.name() : job.getStatus().name(),
                Math.max(0, Math.min(job.getProgressPct(), 100)),
                job.getStage() == null ? CryptoWalletAnalysisStage.FETCH_TX.name() : job.getStage().name(),
                job.getStartedAt(),
                job.getUpdatedAt(),
                job.getFinishedAt(),
                partialReady
        );
    }

    private List<CryptoWalletAnalysisSummaryResponse.AllocationItem> buildAllocation(CryptoWalletSummaryResponse summary) {
        List<CryptoWalletDto> wallets = summary == null || summary.wallets() == null ? List.of() : summary.wallets();
        BigDecimal total = safe(summary == null ? null : summary.totalValueInBase());
        if (total.signum() == 0) {
            total = wallets.stream()
                    .map(CryptoWalletDto::valueInBase)
                    .map(this::safe)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (total.signum() == 0) {
            return List.of();
        }
        BigDecimal totalFinal = total;
        return wallets.stream()
                .filter(w -> w != null && w.valueInBase() != null && w.valueInBase().signum() > 0)
                .map(w -> {
                    BigDecimal value = safe(w.valueInBase());
                    BigDecimal sharePct = value.multiply(BigDecimal.valueOf(100)).divide(totalFinal, 2, RoundingMode.HALF_UP);
                    return new CryptoWalletAnalysisSummaryResponse.AllocationItem(w.network(), value, sharePct);
                })
                .sorted(Comparator.comparing(CryptoWalletAnalysisSummaryResponse.AllocationItem::valueInBase).reversed())
                .toList();
    }

    private BigDecimal computeDeltaPct(Long userId, Instant from, Instant to, BigDecimal totalValue) {
        ReportSummaryResponse summary = reportsService.summary(userId, null, from, to);
        BigDecimal net = safe(summary == null ? null : summary.net());
        BigDecimal denominator = totalValue.signum() > 0 ? totalValue : absOrOne(net);
        if (denominator.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return net.multiply(BigDecimal.valueOf(100)).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private RecurringCandidate detectRecurring(Long userId, String baseCurrency, Instant now) {
        Instant from = now.minus(120, ChronoUnit.DAYS);
        List<Transaction> rows = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, from, now);
        if (rows.isEmpty()) {
            return syntheticRecurring(userId, baseCurrency);
        }

        List<Transaction> expenses = rows.stream()
                .filter(tx -> tx != null
                        && tx.getType() == TransactionType.EXPENSE
                        && tx.getTransactionDate() != null
                        && tx.getAmount() != null
                        && tx.getAmount().signum() > 0)
                .toList();
        if (expenses.size() < 3) {
            return syntheticRecurring(userId, baseCurrency);
        }

        ConversionContext conversion = conversionContext(baseCurrency, expenses.stream().map(Transaction::getCurrency).toList());
        Map<String, List<Transaction>> groups = new HashMap<>();
        for (Transaction tx : expenses) {
            String key = normalizeRecurringKey(tx.getDescription());
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(tx);
        }

        RecurringCandidate best = null;
        for (Map.Entry<String, List<Transaction>> entry : groups.entrySet()) {
            List<Transaction> group = entry.getValue();
            if (group.size() < 3) {
                continue;
            }
            group.sort(Comparator.comparing(Transaction::getTransactionDate));

            int cadenceHits = 0;
            boolean weekly = false;
            for (int i = 1; i < group.size(); i += 1) {
                long days = Duration.between(group.get(i - 1).getTransactionDate(), group.get(i).getTransactionDate()).toDays();
                if (days >= 26 && days <= 33) {
                    cadenceHits += 1;
                } else if (days >= 6 && days <= 8) {
                    cadenceHits += 1;
                    weekly = true;
                }
            }
            if (cadenceHits < 2) {
                continue;
            }

            List<BigDecimal> amounts = group.stream()
                    .map(tx -> convertToBase(tx.getAmount(), tx.getCurrency(), conversion))
                    .toList();
            BigDecimal avg = average(amounts);
            if (avg.signum() <= 0) {
                continue;
            }
            BigDecimal max = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal min = amounts.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal spread = max.subtract(min).divide(avg, 4, RoundingMode.HALF_UP);
            if (spread.compareTo(BigDecimal.valueOf(0.20)) > 0) {
                continue;
            }

            BigDecimal monthlyAmount = weekly
                    ? avg.multiply(BigDecimal.valueOf(4)).setScale(2, RoundingMode.HALF_UP)
                    : avg.setScale(2, RoundingMode.HALF_UP);
            BigDecimal confidence = BigDecimal.valueOf(0.62
                            + Math.min(group.size(), 6) * 0.03
                            + Math.min(cadenceHits, 3) * 0.05
                            + (spread.compareTo(BigDecimal.valueOf(0.10)) <= 0 ? 0.08 : 0))
                    .min(BigDecimal.valueOf(0.98))
                    .setScale(2, RoundingMode.HALF_UP);

            RecurringCandidate candidate = new RecurringCandidate(monthlyAmount, prettifyRecurringLabel(entry.getKey()), confidence, false);
            if (best == null || candidate.amount().compareTo(best.amount()) > 0) {
                best = candidate;
            }
        }

        return best == null ? syntheticRecurring(userId, baseCurrency) : best;
    }

    private RecurringCandidate syntheticRecurring(Long userId, String baseCurrency) {
        ReportSummaryResponse month = reportsService.summary(userId, ReportPeriod.MONTH, null, null);
        BigDecimal expense = safe(month == null ? null : month.expense());
        BigDecimal estimate = expense.multiply(BigDecimal.valueOf(0.26)).setScale(2, RoundingMode.HALF_UP);
        return new RecurringCandidate(estimate, null, BigDecimal.valueOf(0.34), true);
    }

    private AnomalyResult detectAnomalies(Long userId, String baseCurrency, Instant now) {
        Instant from = now.minus(30, ChronoUnit.DAYS);
        List<Transaction> rows = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, from, now);
        List<Transaction> expenses = rows.stream()
                .filter(tx -> tx != null
                        && tx.getType() == TransactionType.EXPENSE
                        && tx.getAmount() != null
                        && tx.getAmount().signum() > 0)
                .toList();
        if (expenses.isEmpty()) {
            return new AnomalyResult(BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP), BigDecimal.valueOf(0.30), true);
        }

        ConversionContext conversion = conversionContext(baseCurrency, expenses.stream().map(Transaction::getCurrency).toList());
        List<BigDecimal> outflows = expenses.stream()
                .map(tx -> convertToBase(tx.getAmount(), tx.getCurrency(), conversion))
                .toList();
        BigDecimal avg = average(outflows);
        BigDecimal threshold = avg.multiply(BigDecimal.valueOf(2.2)).max(BigDecimal.valueOf(50));
        long count = outflows.stream().filter(v -> v.compareTo(threshold) > 0).count();
        BigDecimal confidence = BigDecimal.valueOf(Math.min(0.95, 0.55 + count * 0.12)).setScale(2, RoundingMode.HALF_UP);
        return new AnomalyResult(BigDecimal.valueOf(count).setScale(0, RoundingMode.HALF_UP), confidence, false);
    }

    private void requireWallet(Long userId, Long walletId) {
        if (userId == null || walletId == null) {
            throw walletNotFound();
        }
        cryptoWalletRepository.findByIdAndUserId(walletId, userId).orElseThrow(this::walletNotFound);
    }

    private ConversionContext conversionContext(String baseCurrency, Collection<String> currencies) {
        String base = normalizeCurrency(baseCurrency);
        if (base.isBlank() || !currencyService.isSupported(base)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported base currency", HttpStatus.BAD_REQUEST);
        }

        boolean needCrypto = isCrypto(base) || currencies.stream().anyMatch(this::isCrypto);
        boolean needFx = needsFxRates(base, currencies);
        Map<String, BigDecimal> fxUsd = needFx ? currencyService.latestRates("USD").rates() : Map.of();
        Map<String, BigDecimal> cryptoUsd = needCrypto ? fetchCryptoUsdPrices() : Map.of();

        int scale = isCrypto(base) ? 8 : 2;
        return new ConversionContext(base, scale, fxUsd, cryptoUsd);
    }

    private boolean needsFxRates(String baseCurrency, Collection<String> currencies) {
        if (!isCrypto(baseCurrency) && !"USD".equalsIgnoreCase(baseCurrency)) {
            return true;
        }
        for (String c : currencies) {
            String normalized = normalizeCurrency(c);
            if (!normalized.isBlank() && !isCrypto(normalized) && !"USD".equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal convertToBase(BigDecimal amount, String currency, ConversionContext ctx) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        String from = normalizeCurrency(currency);
        if (from.isBlank() || !currencyService.isSupported(from)) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        if (from.equalsIgnoreCase(ctx.baseCurrency())) {
            return amount.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        BigDecimal usd = toUsd(amount, from, ctx);
        return fromUsd(usd, ctx);
    }

    private BigDecimal toUsd(BigDecimal amount, String currency, ConversionContext ctx) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if ("USD".equalsIgnoreCase(currency)) {
            return amount;
        }
        if (isCrypto(currency)) {
            BigDecimal usdPrice = requireCryptoUsdPrice(ctx.cryptoUsdPrices(), currency);
            return amount.multiply(usdPrice);
        }
        BigDecimal rate = requireFxUsdRate(ctx.usdFxRates(), currency);
        return amount.divide(rate, 12, RoundingMode.HALF_UP);
    }

    private BigDecimal fromUsd(BigDecimal usdAmount, ConversionContext ctx) {
        if (usdAmount == null) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        String base = ctx.baseCurrency();
        if ("USD".equalsIgnoreCase(base)) {
            return usdAmount.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        if (isCrypto(base)) {
            BigDecimal baseUsdPrice = requireCryptoUsdPrice(ctx.cryptoUsdPrices(), base);
            return usdAmount.divide(baseUsdPrice, ctx.scale(), RoundingMode.HALF_UP);
        }
        BigDecimal baseRate = requireFxUsdRate(ctx.usdFxRates(), base);
        return usdAmount.multiply(baseRate).setScale(ctx.scale(), RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> fetchCryptoUsdPrices() {
        CryptoRatesProvider.CryptoRates rates = cryptoRatesService.latestRates("USD");
        if (rates == null || rates.rates() == null) {
            return Map.of();
        }
        Map<String, BigDecimal> prices = new HashMap<>();
        for (var item : rates.rates()) {
            if (item == null || item.code() == null || item.price() == null) {
                continue;
            }
            prices.putIfAbsent(item.code().trim().toUpperCase(Locale.ROOT), item.price());
        }
        return prices;
    }

    private BigDecimal requireFxUsdRate(Map<String, BigDecimal> usdFxRates, String currency) {
        BigDecimal rate = usdFxRates == null ? null : usdFxRates.get(currency);
        if (rate == null || rate.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rate is not available for currency: " + currency, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return rate;
    }

    private BigDecimal requireCryptoUsdPrice(Map<String, BigDecimal> cryptoUsdPrices, String currency) {
        BigDecimal price = cryptoUsdPrices == null ? null : cryptoUsdPrices.get(currency);
        if (price == null || price.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto price is not available for currency: " + currency, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return price;
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = values.stream().map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private String normalizeRecurringKey(String description) {
        if (description == null || description.isBlank()) {
            return "expense";
        }
        return description.trim().toLowerCase(Locale.ROOT);
    }

    private String prettifyRecurringLabel(String key) {
        if (key == null || key.isBlank() || "expense".equals(key)) {
            return null;
        }
        return key.length() <= 1 ? key.toUpperCase(Locale.ROOT) : Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    private BigDecimal safe(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal absOrOne(BigDecimal value) {
        BigDecimal abs = value == null ? BigDecimal.ZERO : value.abs();
        if (abs.signum() == 0) {
            return BigDecimal.ONE;
        }
        return abs;
    }

    private boolean isCrypto(String currency) {
        String normalized = normalizeCurrency(currency);
        return "BTC".equals(normalized) || "ETH".equals(normalized);
    }

    private String normalizeCurrency(String code) {
        if (code == null || code.isBlank()) {
            return "USD";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isTerminal(CryptoWalletAnalysisJobStatus status) {
        return status == CryptoWalletAnalysisJobStatus.DONE || status == CryptoWalletAnalysisJobStatus.FAILED;
    }

    private ApiException walletNotFound() {
        return new ApiException(ErrorCodes.BAD_REQUEST, "Wallet not found", HttpStatus.BAD_REQUEST);
    }

    private record RecurringCandidate(BigDecimal amount, String label, BigDecimal confidence, boolean synthetic) {
    }

    private record AnomalyResult(BigDecimal count, BigDecimal confidence, boolean synthetic) {
    }

    private record ConversionContext(
            String baseCurrency,
            int scale,
            Map<String, BigDecimal> usdFxRates,
            Map<String, BigDecimal> cryptoUsdPrices
    ) {
    }
}
