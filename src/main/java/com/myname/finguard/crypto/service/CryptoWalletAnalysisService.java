package com.myname.finguard.crypto.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.CryptoRatesService;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisInsightItem;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisInsightsResponse;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisSeriesResponse;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisStatusResponse;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisSummaryResponse;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.dto.CryptoWalletSummaryResponse;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJobStatus;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisStage;
import com.myname.finguard.crypto.model.WalletDailySnapshot;
import com.myname.finguard.crypto.model.WalletInsight;
import com.myname.finguard.crypto.model.WalletTxEnriched;
import com.myname.finguard.crypto.model.WalletTxRaw;
import com.myname.finguard.crypto.repository.CryptoWalletAnalysisJobRepository;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import com.myname.finguard.crypto.repository.WalletDailySnapshotRepository;
import com.myname.finguard.crypto.repository.WalletInsightRepository;
import com.myname.finguard.crypto.repository.WalletTxEnrichedRepository;
import com.myname.finguard.crypto.repository.WalletTxRawRepository;
import com.myname.finguard.transactions.model.Transaction;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CryptoWalletAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CryptoWalletAnalysisService.class);
    private static final int DEFAULT_BACKFILL_DAYS = 365;

    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoWalletAnalysisJobRepository jobRepository;
    private final CryptoWalletService cryptoWalletService;
    private final TransactionRepository transactionRepository;
    private final WalletTxRawRepository walletTxRawRepository;
    private final WalletTxEnrichedRepository walletTxEnrichedRepository;
    private final WalletDailySnapshotRepository walletDailySnapshotRepository;
    private final WalletInsightRepository walletInsightRepository;
    private final CurrencyService currencyService;
    private final CryptoRatesService cryptoRatesService;
    private final MeterRegistry meterRegistry;
    private final long simulatedDelayMs;
    private final int backfillDays;
    private final long stalledJobAfterMs;
    private final boolean transactionFallbackEnabled;

    public CryptoWalletAnalysisService(
            CryptoWalletRepository cryptoWalletRepository,
            CryptoWalletAnalysisJobRepository jobRepository,
            @Lazy CryptoWalletService cryptoWalletService,
            TransactionRepository transactionRepository,
            WalletTxRawRepository walletTxRawRepository,
            WalletTxEnrichedRepository walletTxEnrichedRepository,
            WalletDailySnapshotRepository walletDailySnapshotRepository,
            WalletInsightRepository walletInsightRepository,
            CurrencyService currencyService,
            CryptoRatesService cryptoRatesService,
            ObjectProvider<MeterRegistry> meterRegistry,
            @Value("${app.crypto.analysis.simulated-delay-ms:300}") long simulatedDelayMs,
            @Value("${app.crypto.analysis.backfill-days:" + DEFAULT_BACKFILL_DAYS + "}") int backfillDays,
            @Value("${app.crypto.analysis.stalled-after-ms:120000}") long stalledJobAfterMs,
            @Value("${app.crypto.analysis.transaction-fallback-enabled:false}") boolean transactionFallbackEnabled
    ) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.jobRepository = jobRepository;
        this.cryptoWalletService = cryptoWalletService;
        this.transactionRepository = transactionRepository;
        this.walletTxRawRepository = walletTxRawRepository;
        this.walletTxEnrichedRepository = walletTxEnrichedRepository;
        this.walletDailySnapshotRepository = walletDailySnapshotRepository;
        this.walletInsightRepository = walletInsightRepository;
        this.currencyService = currencyService;
        this.cryptoRatesService = cryptoRatesService;
        this.meterRegistry = meterRegistry.getIfAvailable();
        this.simulatedDelayMs = Math.max(0, simulatedDelayMs);
        this.backfillDays = Math.max(30, backfillDays);
        this.stalledJobAfterMs = Math.max(30_000, stalledJobAfterMs);
        this.transactionFallbackEnabled = transactionFallbackEnabled;
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
        job.setLastSuccessfulStage(null);
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
                    false,
                    null,
                    null
            );
        }

        return toResponse(latest);
    }

    public CryptoWalletAnalysisSummaryResponse summary(Long userId, Long walletId) {
        CryptoWallet wallet = requireWallet(userId, walletId);
        Instant now = Instant.now();

        CryptoWalletSummaryResponse walletSummary = cryptoWalletService.walletsSummary(userId);
        String baseCurrency = normalizeCurrency(walletSummary == null ? null : walletSummary.baseCurrency());
        CryptoWalletDto walletDto = findWalletDto(walletSummary, walletId);
        BigDecimal total = safe(walletDto == null ? null : walletDto.valueInBase());

        BigDecimal delta24hPct = computeSnapshotDeltaPct(walletId, baseCurrency, 1);
        BigDecimal delta7dPct = computeSnapshotDeltaPct(walletId, baseCurrency, 7);
        BigDecimal inflow30d = computeSnapshotFlow(walletId, baseCurrency, 30, true);
        BigDecimal outflow30d = computeSnapshotFlow(walletId, baseCurrency, 30, false);
        String metricsSource = resolveSummaryMetricsSource(walletId, now);

        List<CryptoWalletAnalysisSummaryResponse.AllocationItem> allocation = buildAllocationForWallet(walletDto, wallet);
        boolean synthetic = "ESTIMATED".equalsIgnoreCase(metricsSource);
        boolean hasMeaningfulData = total.signum() != 0
                || (inflow30d != null && inflow30d.signum() != 0)
                || (outflow30d != null && outflow30d.signum() != 0)
                || delta24hPct != null
                || delta7dPct != null
                || !allocation.isEmpty();

        recordSynthetic("summary", synthetic);
        int scale = scaleFor(baseCurrency);
        return new CryptoWalletAnalysisSummaryResponse(
                total,
                baseCurrency,
                scalePct(delta24hPct),
                scalePct(delta7dPct),
                inflow30d == null ? null : scaleAmount(inflow30d, scale),
                outflow30d == null ? null : scaleAmount(outflow30d, scale),
                metricsSource,
                hasMeaningfulData,
                allocation,
                now,
                synthetic
        );
    }

    public CryptoWalletAnalysisInsightsResponse insights(Long userId, Long walletId) {
        requireWallet(userId, walletId);
        Instant now = Instant.now();

        List<WalletInsight> persisted = walletInsightRepository.findTop200ByWalletIdOrderByAsOfDescIdDesc(walletId);
        if (!persisted.isEmpty()) {
            List<CryptoWalletAnalysisInsightItem> mapped = mapPersistedInsights(persisted);
            if (!mapped.isEmpty()) {
                Instant asOf = persisted.stream()
                        .map(WalletInsight::getAsOf)
                        .filter(v -> v != null)
                        .max(Instant::compareTo)
                        .orElse(now);
                String base = resolveBaseCurrencyFromPersisted(mapped, userId);
                boolean synthetic = mapped.stream().allMatch(CryptoWalletAnalysisInsightItem::synthetic);
                recordSynthetic("insights", synthetic);
                return new CryptoWalletAnalysisInsightsResponse(base, mapped, asOf);
            }
        }

        CryptoWalletSummaryResponse walletSummary = cryptoWalletService.walletsSummary(userId);
        String baseCurrency = normalizeCurrency(walletSummary == null ? null : walletSummary.baseCurrency());

        recordSynthetic("insights", true);
        return new CryptoWalletAnalysisInsightsResponse(baseCurrency, List.of(), now);
    }

    public CryptoWalletAnalysisSeriesResponse series(Long userId, Long walletId, String window) {
        requireWallet(userId, walletId);
        Instant now = Instant.now();
        int windowDays = parseWindowDays(window);
        String normalizedWindow = normalizeWindow(windowDays);

        CryptoWalletSummaryResponse walletSummary = cryptoWalletService.walletsSummary(userId);
        String baseCurrency = normalizeCurrency(walletSummary == null ? null : walletSummary.baseCurrency());

        List<CryptoWalletAnalysisSeriesResponse.SeriesPoint> fromSnapshots = buildSeriesFromSnapshots(walletId, baseCurrency, windowDays, now);
        if (!fromSnapshots.isEmpty()) {
            boolean synthetic = fromSnapshots.stream().allMatch(point -> point.valueInBase() == null || point.valueInBase().signum() == 0);
            recordSynthetic("series", synthetic);
            return new CryptoWalletAnalysisSeriesResponse(baseCurrency, normalizedWindow, fromSnapshots, now, synthetic);
        }

        recordSynthetic("series", true);
        return new CryptoWalletAnalysisSeriesResponse(baseCurrency, normalizedWindow, List.of(), now, true);
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

    @Scheduled(fixedDelayString = "${app.crypto.analysis.recover-fixed-delay-ms:45000}")
    public void recoverStaleJobs() {
        Instant threshold = Instant.now().minusMillis(stalledJobAfterMs);
        List<CryptoWalletAnalysisJob> stale = jobRepository.findTop100ByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                EnumSet.of(CryptoWalletAnalysisJobStatus.QUEUED, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisJobStatus.PARTIAL),
                threshold
        );
        for (CryptoWalletAnalysisJob job : stale) {
            if (job == null || job.getId() == null) {
                continue;
            }
            log.debug("Resuming stale wallet analysis jobId={} walletId={} stage={}",
                    job.getId(),
                    job.getWallet() == null ? null : job.getWallet().getId(),
                    job.getStage());
            updateJob(job.getId(), CryptoWalletAnalysisJobStatus.QUEUED,
                    job.getStage() == null ? CryptoWalletAnalysisStage.FETCH_TX : job.getStage(),
                    Math.max(0, Math.min(job.getProgressPct(), 98)),
                    null,
                    job.getStartedAt() == null,
                    false,
                    job.getLastSuccessfulStage());
            runAsync(job.getId());
        }
    }

    protected void runPipeline(Long jobId) {
        Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
        CryptoWalletAnalysisStage failedAt = CryptoWalletAnalysisStage.FETCH_TX;
        try {
            CryptoWalletAnalysisJob job = jobRepository.findById(jobId).orElse(null);
            if (job == null || job.getWallet() == null || job.getUser() == null) {
                return;
            }

            Long userId = job.getUser().getId();
            Long walletId = job.getWallet().getId();
            Instant now = Instant.now();
            String baseCurrency = resolveBaseCurrency(userId);
            CryptoWallet wallet = job.getWallet();

            failedAt = CryptoWalletAnalysisStage.FETCH_TX;
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.FETCH_TX, 8, null, true, false, job.getLastSuccessfulStage());
            int rawCount = ingestRawWalletEvents(wallet, baseCurrency, now);
            if (rawCount == 0 && transactionFallbackEnabled) {
                rawCount += ingestRawTransactionsFallback(userId, wallet, baseCurrency, now);
            }
            sleepStep();

            failedAt = CryptoWalletAnalysisStage.ENRICH_TX;
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.ENRICH_TX, 30, null, false, false, CryptoWalletAnalysisStage.FETCH_TX);
            int enrichedCount = enrichTransactions(userId, wallet, now);
            sleepStep();

            failedAt = CryptoWalletAnalysisStage.BUILD_SNAPSHOTS;
            updateJob(jobId, CryptoWalletAnalysisJobStatus.PARTIAL, CryptoWalletAnalysisStage.BUILD_SNAPSHOTS, 58, null, false, false, CryptoWalletAnalysisStage.ENRICH_TX);
            int snapshotsCount = buildSnapshots(userId, walletId, baseCurrency, now);
            sleepStep();

            failedAt = CryptoWalletAnalysisStage.DETECT_RECURRING;
            updateJob(jobId, CryptoWalletAnalysisJobStatus.PARTIAL, CryptoWalletAnalysisStage.DETECT_RECURRING, 76, null, false, false, CryptoWalletAnalysisStage.BUILD_SNAPSHOTS);
            RecurringCandidate recurring = detectRecurringFromEnriched(walletId, baseCurrency, now);
            sleepStep();

            failedAt = CryptoWalletAnalysisStage.BUILD_INSIGHTS;
            updateJob(jobId, CryptoWalletAnalysisJobStatus.RUNNING, CryptoWalletAnalysisStage.BUILD_INSIGHTS, 90, null, false, false, CryptoWalletAnalysisStage.DETECT_RECURRING);
            int insightsCount = persistInsights(userId, walletId, baseCurrency, recurring, now);
            sleepStep();

            updateJob(jobId, CryptoWalletAnalysisJobStatus.DONE, CryptoWalletAnalysisStage.DONE, 100, null, false, true, CryptoWalletAnalysisStage.BUILD_INSIGHTS);

            if (meterRegistry != null) {
                Counter.builder("wallet_analysis_pipeline_rows")
                        .tag("stage", "raw")
                        .register(meterRegistry)
                        .increment(rawCount);
                Counter.builder("wallet_analysis_pipeline_rows")
                        .tag("stage", "enriched")
                        .register(meterRegistry)
                        .increment(enrichedCount);
                Counter.builder("wallet_analysis_pipeline_rows")
                        .tag("stage", "snapshots")
                        .register(meterRegistry)
                        .increment(snapshotsCount);
                Counter.builder("wallet_analysis_pipeline_rows")
                        .tag("stage", "insights")
                        .register(meterRegistry)
                        .increment(insightsCount);
            }
        } catch (Exception ex) {
            updateJob(jobId, CryptoWalletAnalysisJobStatus.FAILED, failedAt, 100, "Analysis failed", false, true, null);
            if (meterRegistry != null) {
                Counter.builder("wallet_analysis_fail_rate").register(meterRegistry).increment();
            }
            log.debug("Wallet analysis pipeline failed for jobId={}: {}", jobId, ex.getMessage());
        } finally {
            if (sample != null) {
                sample.stop(meterRegistry.timer("wallet_analysis_duration"));
            }
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
        updateJob(jobId, status, stage, progressPct, errorMessage, markStarted, markFinished, null);
    }

    protected void updateJob(
            Long jobId,
            CryptoWalletAnalysisJobStatus status,
            CryptoWalletAnalysisStage stage,
            int progressPct,
            String errorMessage,
            boolean markStarted,
            boolean markFinished,
            CryptoWalletAnalysisStage lastSuccessfulStage
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
        if (lastSuccessfulStage != null) {
            job.setLastSuccessfulStage(lastSuccessfulStage);
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

    private int ingestRawWalletEvents(CryptoWallet wallet, String baseCurrency, Instant now) {
        // Live wallet events provider is not wired yet in this iteration.
        // Returning 0 keeps pipeline deterministic; optional transaction fallback remains emergency-only.
        return 0;
    }

    private int ingestRawTransactionsFallback(Long userId, CryptoWallet wallet, String baseCurrency, Instant now) {
        Instant from = now.minus(backfillDays, ChronoUnit.DAYS);
        List<Transaction> rows = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, from, now);
        if (rows.isEmpty()) {
            return 0;
        }

        ConversionContext conversion = conversionContext(baseCurrency, rows.stream().map(Transaction::getCurrency).toList());
        int persisted = 0;
        for (Transaction tx : rows) {
            if (tx == null || tx.getTransactionDate() == null || tx.getAmount() == null || tx.getType() == null) {
                continue;
            }
            String txHash = "txn-" + tx.getId();
            long logIndex = 0;
            WalletTxRaw raw = walletTxRawRepository.findByWalletIdAndTxHashAndLogIndex(wallet.getId(), txHash, logIndex).orElseGet(WalletTxRaw::new);
            raw.setUser(wallet.getUser());
            raw.setWallet(wallet);
            raw.setNetwork(wallet.getNetwork() == null ? "EVM" : wallet.getNetwork().name());
            raw.setTxHash(txHash);
            raw.setLogIndex(logIndex);
            raw.setTxAt(tx.getTransactionDate());
            raw.setBlockNumber(null);
            raw.setDirection(tx.getType() == TransactionType.INCOME ? "IN" : "OUT");
            raw.setAssetCode(normalizeCurrency(tx.getCurrency()));
            raw.setAmount(abs(tx.getAmount()));
            raw.setAmountUsd(scaleAmount(toUsd(abs(tx.getAmount()), tx.getCurrency(), conversion), 8));
            raw.setFeeUsd(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_UP));
            raw.setCounterparty(normalizeCounterparty(tx.getDescription()));
            raw.setSource("TRANSACTION_FALLBACK");
            raw.setRawPayload(null);
            walletTxRawRepository.save(raw);
            persisted += 1;
        }
        return persisted;
    }

    private int enrichTransactions(Long userId, CryptoWallet wallet, Instant now) {
        Instant from = now.minus(backfillDays, ChronoUnit.DAYS);
        List<WalletTxRaw> rawRows = walletTxRawRepository.findByWalletIdAndTxAtBetweenOrderByTxAtAsc(wallet.getId(), from, now);
        if (rawRows.isEmpty()) {
            return 0;
        }

        int persisted = 0;
        for (WalletTxRaw raw : rawRows) {
            if (raw == null || raw.getTxHash() == null) {
                continue;
            }
            WalletTxEnriched enriched = walletTxEnrichedRepository
                    .findByWalletIdAndTxHashAndLogIndex(wallet.getId(), raw.getTxHash(), raw.getLogIndex())
                    .orElseGet(WalletTxEnriched::new);

            enriched.setUser(wallet.getUser());
            enriched.setWallet(wallet);
            enriched.setRawTx(raw);
            enriched.setTxHash(raw.getTxHash());
            enriched.setLogIndex(raw.getLogIndex());
            enriched.setTxAt(raw.getTxAt());
            enriched.setDirection(raw.getDirection());
            enriched.setAssetCode(raw.getAssetCode());
            enriched.setAmount(scaleAmount(raw.getAmount(), 8));
            enriched.setAmountUsd(scaleAmount(raw.getAmountUsd(), 8));
            enriched.setCategory("OUT".equalsIgnoreCase(raw.getDirection()) ? "expense" : "income");
            enriched.setCounterpartyNormalized(normalizeRecurringKey(raw.getCounterparty()));
            enriched.setRecurringCandidate(false);
            enriched.setConfidence(BigDecimal.valueOf(0.5).setScale(4, RoundingMode.HALF_UP));
            enriched.setSource(raw.getSource() == null ? "ESTIMATED" : raw.getSource());
            walletTxEnrichedRepository.save(enriched);
            persisted += 1;
        }
        return persisted;
    }

    private int buildSnapshots(Long userId, Long walletId, String baseCurrency, Instant now) {
        Instant from = now.minus(backfillDays, ChronoUnit.DAYS);
        List<WalletTxEnriched> enrichedRows = walletTxEnrichedRepository.findByWalletIdAndTxAtBetweenOrderByTxAtAsc(walletId, from, now);
        CryptoWallet wallet = cryptoWalletRepository.findByIdAndUserId(walletId, userId).orElseThrow(this::walletNotFound);
        String snapshotSource = resolveSnapshotSource(enrichedRows);

        Map<LocalDate, BigDecimal> inflowUsdByDay = new HashMap<>();
        Map<LocalDate, BigDecimal> outflowUsdByDay = new HashMap<>();
        for (WalletTxEnriched tx : enrichedRows) {
            if (tx == null || tx.getTxAt() == null) {
                continue;
            }
            LocalDate day = LocalDate.ofInstant(tx.getTxAt(), ZoneOffset.UTC);
            BigDecimal amountUsd = scaleAmount(tx.getAmountUsd(), 8);
            if (amountUsd.signum() == 0) {
                continue;
            }
            if ("OUT".equalsIgnoreCase(tx.getDirection())) {
                outflowUsdByDay.merge(day, amountUsd.abs(), BigDecimal::add);
            } else {
                inflowUsdByDay.merge(day, amountUsd.abs(), BigDecimal::add);
            }
        }

        BigDecimal currentValueUsd = currentWalletUsd(userId, walletId, baseCurrency);
        LocalDate endDay = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate startDay = endDay.minusDays(backfillDays - 1L);

        Map<LocalDate, WalletDailySnapshot> snapshots = new LinkedHashMap<>();
        BigDecimal running = currentValueUsd;
        for (LocalDate day = endDay; !day.isBefore(startDay); day = day.minusDays(1)) {
            BigDecimal inflow = scaleAmount(inflowUsdByDay.get(day), 8);
            BigDecimal outflow = scaleAmount(outflowUsdByDay.get(day), 8);
            BigDecimal net = inflow.subtract(outflow).setScale(8, RoundingMode.HALF_UP);

            WalletDailySnapshot snapshot = walletDailySnapshotRepository.findByWalletIdAndDay(walletId, day)
                    .orElseGet(WalletDailySnapshot::new);
            snapshot.setUser(wallet.getUser());
            snapshot.setWallet(wallet);
            snapshot.setDay(day);
            snapshot.setPortfolioUsd(scaleAmount(running.max(BigDecimal.ZERO), 8));
            snapshot.setInflowUsd(inflow);
            snapshot.setOutflowUsd(outflow);
            snapshot.setNetFlowUsd(net);
            snapshot.setPnlUsd(net);
            snapshot.setPnlPct(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
            snapshot.setSource(snapshotSource);
            snapshots.put(day, snapshot);

            running = running.subtract(net);
        }

        for (WalletDailySnapshot snapshot : snapshots.values()) {
            walletDailySnapshotRepository.save(snapshot);
        }
        return snapshots.size();
    }

    private String resolveSnapshotSource(List<WalletTxEnriched> rows) {
        if (rows == null || rows.isEmpty()) {
            return "ESTIMATED";
        }
        boolean hasFallback = false;
        boolean hasReliable = false;
        for (WalletTxEnriched row : rows) {
            String source = normalizeInsightSource(row == null ? null : row.getSource());
            if ("ESTIMATED".equals(source)) {
                return "ESTIMATED";
            }
            if ("TRANSACTION_FALLBACK".equals(source)) {
                hasFallback = true;
                continue;
            }
            hasReliable = true;
        }
        if (hasFallback && !hasReliable) {
            return "TRANSACTION_FALLBACK";
        }
        if (hasReliable) {
            return "PARTIAL";
        }
        return "ESTIMATED";
    }

    private int persistInsights(Long userId, Long walletId, String baseCurrency, RecurringCandidate recurring, Instant now) {
        walletInsightRepository.deleteByWalletId(walletId);

        List<WalletInsight> items = new ArrayList<>();
        List<WalletTxEnriched> thirtyDays = walletTxEnrichedRepository.findByWalletIdAndTxAtBetweenOrderByTxAtAsc(
                walletId,
                now.minus(30, ChronoUnit.DAYS),
                now
        );

        WalletInsight topOutflow = new WalletInsight();
        topOutflow.setInsightType("TOP_OUTFLOW");
        topOutflow.setTitle("Top outflow");
        WalletTxEnriched maxOut = thirtyDays.stream()
                .filter(tx -> tx != null && "OUT".equalsIgnoreCase(tx.getDirection()))
                .max(Comparator.comparing(tx -> scaleAmount(tx.getAmountUsd(), 8)))
                .orElse(null);
        if (maxOut != null) {
            topOutflow.setValue(scaleAmount(convertUsdToBase(scaleAmount(maxOut.getAmountUsd(), 8), baseCurrency), 2));
            topOutflow.setUnit("BASE_CURRENCY");
            topOutflow.setCurrency(baseCurrency);
            topOutflow.setLabel(firstNonBlank(maxOut.getCounterpartyNormalized(), "Outflow"));
            topOutflow.setConfidence(BigDecimal.valueOf(0.88).setScale(4, RoundingMode.HALF_UP));
            topOutflow.setSynthetic(false);
            topOutflow.setSource(normalizeInsightSource(maxOut.getSource()));
            topOutflow.setAsOf(now);
            items.add(topOutflow);
        }

        if (recurring != null
                && recurring.amount() != null
                && recurring.amount().signum() > 0
                && recurring.confidence() != null
                && recurring.confidence().compareTo(BigDecimal.ZERO) > 0) {
            WalletInsight recurringInsight = new WalletInsight();
            recurringInsight.setInsightType("RECURRING_SPEND");
            recurringInsight.setTitle("Recurring spend");
            recurringInsight.setValue(scaleAmount(recurring.amount(), 2));
            recurringInsight.setUnit("BASE_CURRENCY");
            recurringInsight.setCurrency(baseCurrency);
            recurringInsight.setLabel(recurring.label());
            recurringInsight.setAvgAmount(scaleAmount(recurring.avgAmount(), 2));
            recurringInsight.setNextEstimatedChargeAt(recurring.nextEstimatedChargeAt());
            recurringInsight.setConfidence(scaleAmount(recurring.confidence(), 4));
            recurringInsight.setSynthetic(false);
            recurringInsight.setSource(normalizeInsightSource(recurring.source()));
            recurringInsight.setAsOf(now);
            items.add(recurringInsight);
        }

        BigDecimal trend = computeWalletTrend30dPct(walletId, baseCurrency);
        if (trend != null) {
            WalletInsight trendInsight = new WalletInsight();
            trendInsight.setInsightType("PORTFOLIO_30D_CHANGE");
            trendInsight.setTitle("30d portfolio trend");
            trendInsight.setValue(scaleAmount(trend, 2));
            trendInsight.setUnit("PERCENT");
            trendInsight.setCurrency(null);
            trendInsight.setConfidence(BigDecimal.valueOf(0.72).setScale(4, RoundingMode.HALF_UP));
            trendInsight.setSynthetic(false);
            trendInsight.setSource(normalizeInsightSource(resolveSummaryMetricsSource(walletId, now)));
            trendInsight.setAsOf(now);
            items.add(trendInsight);
        }

        AnomalyResult anomalies = detectAnomaliesFromEnriched(walletId);
        if (anomalies != null && anomalies.count() != null && anomalies.count().signum() > 0) {
            WalletInsight anomaliesInsight = new WalletInsight();
            anomaliesInsight.setInsightType("ANOMALOUS_OUTFLOWS");
            anomaliesInsight.setTitle("Anomalous outflows");
            anomaliesInsight.setValue(scaleAmount(anomalies.count(), 0));
            anomaliesInsight.setUnit("COUNT");
            anomaliesInsight.setCurrency(null);
            anomaliesInsight.setConfidence(scaleAmount(anomalies.confidence(), 4));
            anomaliesInsight.setSynthetic(false);
            anomaliesInsight.setSource(normalizeInsightSource(anomalies.source()));
            anomaliesInsight.setAsOf(now);
            items.add(anomaliesInsight);
        }

        CryptoWallet wallet = cryptoWalletRepository.findByIdAndUserId(walletId, userId).orElseThrow(this::walletNotFound);
        for (WalletInsight item : items) {
            item.setUser(wallet.getUser());
            item.setWallet(wallet);
            if (item.getSource() == null || item.getSource().isBlank()) {
                item.setSource("PARTIAL");
            }
            walletInsightRepository.save(item);
        }
        return items.size();
    }

    private List<CryptoWalletAnalysisSeriesResponse.SeriesPoint> buildSeriesFromSnapshots(Long walletId, String baseCurrency, int windowDays, Instant now) {
        LocalDate endDay = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate startDay = endDay.minusDays(windowDays - 1L);
        List<WalletDailySnapshot> rows = walletDailySnapshotRepository.findByWalletIdAndDayBetweenOrderByDayAsc(walletId, startDay, endDay);
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, WalletDailySnapshot> byDay = new HashMap<>();
        for (WalletDailySnapshot row : rows) {
            if (row == null || row.getDay() == null) {
                continue;
            }
            byDay.put(row.getDay(), row);
        }

        List<CryptoWalletAnalysisSeriesResponse.SeriesPoint> points = new ArrayList<>(windowDays);
        BigDecimal previous = BigDecimal.ZERO;
        int scale = scaleFor(baseCurrency);
        for (LocalDate day = startDay; !day.isAfter(endDay); day = day.plusDays(1)) {
            WalletDailySnapshot snapshot = byDay.get(day);
            BigDecimal value = snapshot == null ? previous : convertUsdToBase(scaleAmount(snapshot.getPortfolioUsd(), 8), baseCurrency);
            value = scaleAmount(value, scale);
            points.add(new CryptoWalletAnalysisSeriesResponse.SeriesPoint(day.atStartOfDay().toInstant(ZoneOffset.UTC), value));
            previous = value;
        }
        return points;
    }

    private List<CryptoWalletAnalysisInsightItem> mapPersistedInsights(List<WalletInsight> persisted) {
        if (persisted == null || persisted.isEmpty()) {
            return List.of();
        }
        Map<String, WalletInsight> latestByType = new LinkedHashMap<>();
        for (WalletInsight item : persisted) {
            if (item == null
                    || item.getInsightType() == null
                    || item.isSynthetic()
                    || latestByType.containsKey(item.getInsightType())) {
                continue;
            }
            latestByType.put(item.getInsightType(), item);
        }

        List<CryptoWalletAnalysisInsightItem> result = new ArrayList<>(latestByType.size());
        for (WalletInsight insight : latestByType.values()) {
            result.add(new CryptoWalletAnalysisInsightItem(
                    insight.getInsightType(),
                    insight.getTitle(),
                    insight.getValue(),
                    insight.getUnit(),
                    insight.getCurrency(),
                    insight.getLabel(),
                    insight.getAvgAmount(),
                    insight.getNextEstimatedChargeAt(),
                    insight.getConfidence(),
                    insight.getAsOf(),
                    insight.isSynthetic()
            ));
        }
        return result;
    }

    private RecurringCandidate detectRecurringFromEnriched(Long walletId, String baseCurrency, Instant now) {
        Instant from = now.minus(120, ChronoUnit.DAYS);
        List<WalletTxEnriched> rows = walletTxEnrichedRepository.findByWalletIdAndTxAtBetweenOrderByTxAtAsc(walletId, from, now);
        List<WalletTxEnriched> expenses = rows.stream()
                .filter(tx -> tx != null
                        && tx.getTxAt() != null
                        && "OUT".equalsIgnoreCase(tx.getDirection())
                        && tx.getAmountUsd() != null
                        && tx.getAmountUsd().signum() > 0)
                .toList();
        if (expenses.size() < 3) {
            return null;
        }

        Map<String, List<WalletTxEnriched>> groups = new HashMap<>();
        for (WalletTxEnriched tx : expenses) {
            String key = normalizeRecurringKey(firstNonBlank(tx.getCounterpartyNormalized(), tx.getCategory(), "expense"));
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(tx);
        }

        RecurringCandidate best = null;
        for (Map.Entry<String, List<WalletTxEnriched>> entry : groups.entrySet()) {
            List<WalletTxEnriched> group = entry.getValue();
            if (group.size() < 3) {
                continue;
            }
            group.sort(Comparator.comparing(WalletTxEnriched::getTxAt));

            int cadenceHits = 0;
            long cadenceDaysSum = 0;
            int cadenceSamples = 0;
            for (int i = 1; i < group.size(); i += 1) {
                long days = Duration.between(group.get(i - 1).getTxAt(), group.get(i).getTxAt()).toDays();
                if (days >= 26 && days <= 33) {
                    cadenceHits += 1;
                    cadenceDaysSum += days;
                    cadenceSamples += 1;
                } else if (days >= 6 && days <= 8) {
                    cadenceHits += 1;
                    cadenceDaysSum += days;
                    cadenceSamples += 1;
                }
            }
            if (cadenceHits < 2) {
                continue;
            }

            List<BigDecimal> amounts = group.stream().map(tx -> scaleAmount(tx.getAmountUsd(), 8)).toList();
            BigDecimal avgUsd = average(amounts);
            if (avgUsd.signum() <= 0) {
                continue;
            }
            BigDecimal max = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal min = amounts.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal spread = max.subtract(min).divide(avgUsd, 4, RoundingMode.HALF_UP);
            if (spread.compareTo(BigDecimal.valueOf(0.20)) > 0) {
                continue;
            }

            long cadenceDays = cadenceSamples <= 0 ? 30 : Math.max(1, Math.round((double) cadenceDaysSum / cadenceSamples));
            BigDecimal monthlyMultiplier = BigDecimal.valueOf(30.0 / cadenceDays);
            BigDecimal monthlyUsd = avgUsd.multiply(monthlyMultiplier).setScale(8, RoundingMode.HALF_UP);
            BigDecimal monthlyBase = convertUsdToBase(monthlyUsd, baseCurrency).setScale(2, RoundingMode.HALF_UP);
            BigDecimal avgBase = convertUsdToBase(avgUsd, baseCurrency).setScale(2, RoundingMode.HALF_UP);
            Instant lastCharge = group.get(group.size() - 1).getTxAt();
            Instant nextEstimatedChargeAt = lastCharge == null ? now.plus(14, ChronoUnit.DAYS) : lastCharge.plus(cadenceDays, ChronoUnit.DAYS);
            BigDecimal confidence = BigDecimal.valueOf(0.62
                            + Math.min(group.size(), 6) * 0.03
                            + Math.min(cadenceHits, 3) * 0.05
                            + (spread.compareTo(BigDecimal.valueOf(0.10)) <= 0 ? 0.08 : 0))
                    .min(BigDecimal.valueOf(0.98))
                    .setScale(2, RoundingMode.HALF_UP);
            String source = resolveRecurringSource(group);

            RecurringCandidate candidate = new RecurringCandidate(
                    monthlyBase,
                    avgBase,
                    prettifyRecurringLabel(entry.getKey()),
                    confidence,
                    nextEstimatedChargeAt,
                    source
            );
            if (best == null || candidate.amount().compareTo(best.amount()) > 0) {
                best = candidate;
            }
        }

        return best;
    }

    private String resolveRecurringSource(List<WalletTxEnriched> rows) {
        if (rows == null || rows.isEmpty()) {
            return "ESTIMATED";
        }
        boolean allFallback = true;
        for (WalletTxEnriched row : rows) {
            String source = normalizeInsightSource(row == null ? null : row.getSource());
            if (!"TRANSACTION_FALLBACK".equals(source)) {
                allFallback = false;
                break;
            }
        }
        return allFallback ? "TRANSACTION_FALLBACK" : "PARTIAL";
    }

    private AnomalyResult detectAnomaliesFromEnriched(Long walletId) {
        Instant now = Instant.now();
        Instant from = now.minus(30, ChronoUnit.DAYS);
        List<WalletTxEnriched> rows = walletTxEnrichedRepository.findByWalletIdAndTxAtBetweenOrderByTxAtAsc(walletId, from, now);
        List<BigDecimal> outflows = rows.stream()
                .filter(tx -> tx != null && "OUT".equalsIgnoreCase(tx.getDirection()) && tx.getAmountUsd() != null)
                .map(WalletTxEnriched::getAmountUsd)
                .map(this::scaleUsd)
                .filter(value -> value.signum() > 0)
                .toList();
        if (outflows.isEmpty()) {
            return null;
        }

        BigDecimal avg = average(outflows);
        BigDecimal threshold = avg.multiply(BigDecimal.valueOf(2.2)).max(BigDecimal.valueOf(50));
        long count = outflows.stream().filter(v -> v.compareTo(threshold) > 0).count();
        BigDecimal confidence = BigDecimal.valueOf(Math.min(0.95, 0.55 + count * 0.12)).setScale(2, RoundingMode.HALF_UP);
        String source = resolveRecurringSource(rows);
        return new AnomalyResult(BigDecimal.valueOf(count).setScale(0, RoundingMode.HALF_UP), confidence, source);
    }

    private BigDecimal computeWalletTrend30dPct(Long walletId, String baseCurrency) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        WalletDailySnapshot current = walletDailySnapshotRepository.findTopByWalletIdAndDayBeforeOrderByDayDesc(walletId, today.plusDays(1)).orElse(null);
        WalletDailySnapshot previous = walletDailySnapshotRepository.findTopByWalletIdAndDayBeforeOrderByDayDesc(walletId, today.minusDays(29)).orElse(null);
        if (current == null || previous == null || current.getPortfolioUsd() == null || previous.getPortfolioUsd() == null) {
            return null;
        }
        if ("ESTIMATED".equalsIgnoreCase(current.getSource())
                || "ESTIMATED".equalsIgnoreCase(previous.getSource())
                || "TRANSACTION_FALLBACK".equalsIgnoreCase(current.getSource())
                || "TRANSACTION_FALLBACK".equalsIgnoreCase(previous.getSource())) {
            return null;
        }

        BigDecimal curBase = convertUsdToBase(scaleAmount(current.getPortfolioUsd(), 8), baseCurrency);
        BigDecimal prevBase = convertUsdToBase(scaleAmount(previous.getPortfolioUsd(), 8), baseCurrency);
        BigDecimal denominator = prevBase.abs().max(BigDecimal.ONE);
        return curBase.subtract(prevBase)
                .multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSnapshotDeltaPct(Long walletId, String baseCurrency, int daysBack) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        WalletDailySnapshot nowSnapshot = walletDailySnapshotRepository.findTopByWalletIdAndDayBeforeOrderByDayDesc(walletId, today.plusDays(1)).orElse(null);
        WalletDailySnapshot prevSnapshot = walletDailySnapshotRepository.findTopByWalletIdAndDayBeforeOrderByDayDesc(walletId, today.minusDays(daysBack - 1L)).orElse(null);
        if (nowSnapshot == null || prevSnapshot == null || nowSnapshot.getPortfolioUsd() == null || prevSnapshot.getPortfolioUsd() == null) {
            return null;
        }

        BigDecimal cur = convertUsdToBase(scaleAmount(nowSnapshot.getPortfolioUsd(), 8), baseCurrency);
        BigDecimal prev = convertUsdToBase(scaleAmount(prevSnapshot.getPortfolioUsd(), 8), baseCurrency);
        if (prev == null || prev.signum() == 0) {
            return null;
        }
        return cur.subtract(prev)
                .multiply(BigDecimal.valueOf(100))
                .divide(prev.abs(), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSnapshotFlow(Long walletId, String baseCurrency, int daysBack, boolean inflow) {
        LocalDate endDay = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDay = endDay.minusDays(Math.max(1, daysBack) - 1L);
        List<WalletDailySnapshot> rows = walletDailySnapshotRepository.findByWalletIdAndDayBetweenOrderByDayAsc(walletId, startDay, endDay);
        if (rows.isEmpty()) {
            return null;
        }
        BigDecimal totalUsd = BigDecimal.ZERO;
        for (WalletDailySnapshot row : rows) {
            if (row == null) {
                continue;
            }
            BigDecimal valueUsd = inflow ? row.getInflowUsd() : row.getOutflowUsd();
            if (valueUsd == null) {
                continue;
            }
            totalUsd = totalUsd.add(scaleAmount(valueUsd, 8).abs());
        }
        if (totalUsd.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return convertUsdToBase(scaleAmount(totalUsd, 8), baseCurrency);
    }

    private String resolveSummaryMetricsSource(Long walletId, Instant now) {
        LocalDate endDay = LocalDate.ofInstant(now == null ? Instant.now() : now, ZoneOffset.UTC);
        LocalDate startDay = endDay.minusDays(29);
        List<WalletDailySnapshot> rows = walletDailySnapshotRepository.findByWalletIdAndDayBetweenOrderByDayAsc(walletId, startDay, endDay);
        if (rows.isEmpty()) {
            return "ESTIMATED";
        }
        boolean hasLive = false;
        boolean hasPartial = false;
        for (WalletDailySnapshot row : rows) {
            String source = normalizeInsightSource(row == null ? null : row.getSource());
            if ("ESTIMATED".equals(source) || "TRANSACTION_FALLBACK".equals(source)) {
                return "ESTIMATED";
            }
            if ("PARTIAL".equals(source)) {
                hasPartial = true;
                continue;
            }
            if ("LIVE".equals(source)) {
                hasLive = true;
                continue;
            }
            return "ESTIMATED";
        }
        if (hasPartial) {
            return "PARTIAL";
        }
        return hasLive ? "LIVE" : "ESTIMATED";
    }

    private BigDecimal currentWalletUsd(Long userId, Long walletId, String baseCurrency) {
        CryptoWalletSummaryResponse summary = cryptoWalletService.walletsSummary(userId);
        CryptoWalletDto dto = findWalletDto(summary, walletId);
        BigDecimal inBase = safe(dto == null ? null : dto.valueInBase());
        return scaleUsd(toUsd(inBase, baseCurrency, conversionContext(baseCurrency, List.of(baseCurrency))));
    }

    private CryptoWalletDto findWalletDto(CryptoWalletSummaryResponse walletSummary, Long walletId) {
        if (walletSummary == null || walletSummary.wallets() == null || walletId == null) {
            return null;
        }
        return walletSummary.wallets().stream()
                .filter(item -> item != null && walletId.equals(item.id()))
                .findFirst()
                .orElse(null);
    }

    private String resolveBaseCurrencyFromPersisted(List<CryptoWalletAnalysisInsightItem> items, Long userId) {
        for (CryptoWalletAnalysisInsightItem item : items) {
            if (item != null && item.currency() != null && !item.currency().isBlank()) {
                return normalizeCurrency(item.currency());
            }
        }
        return resolveBaseCurrency(userId);
    }

    private String resolveBaseCurrency(Long userId) {
        CryptoWalletSummaryResponse summary = cryptoWalletService.walletsSummary(userId);
        return normalizeCurrency(summary == null ? null : summary.baseCurrency());
    }

    private List<CryptoWalletAnalysisSummaryResponse.AllocationItem> buildAllocationForWallet(CryptoWalletDto walletDto, CryptoWallet wallet) {
        BigDecimal value = safe(walletDto == null ? null : walletDto.valueInBase());
        if (value.signum() <= 0) {
            return List.of();
        }
        String code = walletDto != null && walletDto.network() != null
                ? walletDto.network()
                : (wallet.getNetwork() == null ? "WALLET" : wallet.getNetwork().name());
        return List.of(new CryptoWalletAnalysisSummaryResponse.AllocationItem(
                code,
                value,
                BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP)
        ));
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
                || job.getStatus() == CryptoWalletAnalysisJobStatus.DONE
                || isStageAtOrPast(job.getLastSuccessfulStage(), CryptoWalletAnalysisStage.BUILD_SNAPSHOTS);

        return new CryptoWalletAnalysisStatusResponse(
                job.getStatus() == null ? CryptoWalletAnalysisJobStatus.QUEUED.name() : job.getStatus().name(),
                Math.max(0, Math.min(job.getProgressPct(), 100)),
                job.getStage() == null ? CryptoWalletAnalysisStage.FETCH_TX.name() : job.getStage().name(),
                job.getStartedAt(),
                job.getUpdatedAt(),
                job.getFinishedAt(),
                partialReady,
                estimateEtaSeconds(job),
                job.getLastSuccessfulStage() == null ? null : job.getLastSuccessfulStage().name()
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

    private boolean isStageAtOrPast(CryptoWalletAnalysisStage stage, CryptoWalletAnalysisStage pivot) {
        if (stage == null || pivot == null) {
            return false;
        }
        return stage.ordinal() >= pivot.ordinal();
    }

    private int parseWindowDays(String window) {
        String normalized = window == null ? "" : window.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "7d" -> 7;
            case "", "30d" -> 30;
            case "90d" -> 90;
            case "1y", "365d" -> 365;
            default -> throw new ApiException(
                    ErrorCodes.BAD_REQUEST,
                    "Unsupported analysis window. Use one of: 7d, 30d, 90d, 1y",
                    HttpStatus.BAD_REQUEST
            );
        };
    }

    private String normalizeWindow(int days) {
        if (days == 7) {
            return "7d";
        }
        if (days == 90) {
            return "90d";
        }
        if (days >= 365) {
            return "1y";
        }
        return "30d";
    }

    private CryptoWallet requireWallet(Long userId, Long walletId) {
        if (userId == null || walletId == null) {
            throw walletNotFound();
        }
        return cryptoWalletRepository.findByIdAndUserId(walletId, userId).orElseThrow(this::walletNotFound);
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

        int scale = scaleFor(base);
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

    private BigDecimal convertUsdToBase(BigDecimal usdAmount, String baseCurrency) {
        String base = normalizeCurrency(baseCurrency);
        ConversionContext ctx = conversionContext(base, List.of("USD", base));
        return fromUsd(scaleUsd(usdAmount), ctx);
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

    private BigDecimal scalePct(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleUsd(BigDecimal value) {
        return scaleAmount(value, 8);
    }

    private BigDecimal scaleAmount(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private String normalizeCounterparty(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed.length() <= 160 ? trimmed : trimmed.substring(0, 160);
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

    private BigDecimal abs(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.abs();
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

    private int scaleFor(String currency) {
        return isCrypto(currency) ? 8 : 2;
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

    private String normalizeInsightSource(String source) {
        String normalized = source == null ? "" : source.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "ESTIMATED";
        }
        if ("LIVE".equals(normalized)
                || "PARTIAL".equals(normalized)
                || "ESTIMATED".equals(normalized)
                || "TRANSACTION_FALLBACK".equals(normalized)) {
            return normalized;
        }
        return "ESTIMATED";
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private boolean isTerminal(CryptoWalletAnalysisJobStatus status) {
        return status == CryptoWalletAnalysisJobStatus.DONE || status == CryptoWalletAnalysisJobStatus.FAILED;
    }

    private ApiException walletNotFound() {
        return new ApiException(ErrorCodes.BAD_REQUEST, "Wallet not found", HttpStatus.BAD_REQUEST);
    }

    private void recordSynthetic(String surface, boolean synthetic) {
        if (meterRegistry == null) {
            return;
        }
        Counter.builder("synthetic_ratio")
                .tag("surface", surface)
                .tag("synthetic", synthetic ? "true" : "false")
                .register(meterRegistry)
                .increment();
    }

    private record RecurringCandidate(
            BigDecimal amount,
            BigDecimal avgAmount,
            String label,
            BigDecimal confidence,
            Instant nextEstimatedChargeAt,
            String source
    ) {
    }

    private record AnomalyResult(BigDecimal count, BigDecimal confidence, String source) {
    }

    private record ConversionContext(
            String baseCurrency,
            int scale,
            Map<String, BigDecimal> usdFxRates,
            Map<String, BigDecimal> cryptoUsdPrices
    ) {
    }
}
