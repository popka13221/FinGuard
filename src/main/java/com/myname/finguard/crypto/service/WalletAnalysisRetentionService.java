package com.myname.finguard.crypto.service;

import com.myname.finguard.crypto.repository.WalletDailySnapshotRepository;
import com.myname.finguard.crypto.repository.WalletInsightRepository;
import com.myname.finguard.crypto.repository.WalletTxEnrichedRepository;
import com.myname.finguard.crypto.repository.WalletTxRawRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletAnalysisRetentionService {

    private static final Logger log = LoggerFactory.getLogger(WalletAnalysisRetentionService.class);

    private final WalletTxRawRepository walletTxRawRepository;
    private final WalletTxEnrichedRepository walletTxEnrichedRepository;
    private final WalletDailySnapshotRepository walletDailySnapshotRepository;
    private final WalletInsightRepository walletInsightRepository;
    private final MeterRegistry meterRegistry;

    public WalletAnalysisRetentionService(
            WalletTxRawRepository walletTxRawRepository,
            WalletTxEnrichedRepository walletTxEnrichedRepository,
            WalletDailySnapshotRepository walletDailySnapshotRepository,
            WalletInsightRepository walletInsightRepository,
            ObjectProvider<MeterRegistry> meterRegistry
    ) {
        this.walletTxRawRepository = walletTxRawRepository;
        this.walletTxEnrichedRepository = walletTxEnrichedRepository;
        this.walletDailySnapshotRepository = walletDailySnapshotRepository;
        this.walletInsightRepository = walletInsightRepository;
        this.meterRegistry = meterRegistry.getIfAvailable();
    }

    @Scheduled(cron = "${app.crypto.analysis.retention.cron:0 17 3 * * *}")
    @Transactional
    public void cleanupRetention() {
        Instant now = Instant.now();
        Instant rawThreshold = now.minusSeconds(90L * 24L * 60L * 60L);
        Instant insightsThreshold = now.minusSeconds(365L * 24L * 60L * 60L);
        LocalDate snapshotThreshold = LocalDate.now(ZoneOffset.UTC).minusDays(365);

        int rawDeleted = walletTxRawRepository.deleteOlderThan(rawThreshold);
        int enrichedDeleted = walletTxEnrichedRepository.deleteOlderThan(rawThreshold);
        int snapshotDeleted = walletDailySnapshotRepository.deleteByDayBefore(snapshotThreshold);
        int insightDeleted = walletInsightRepository.deleteByAsOfBefore(insightsThreshold);

        record("wallet_tx_raw", rawDeleted);
        record("wallet_tx_enriched", enrichedDeleted);
        record("wallet_daily_snapshots", snapshotDeleted);
        record("wallet_insights", insightDeleted);

        if (rawDeleted + enrichedDeleted + snapshotDeleted + insightDeleted > 0) {
            log.debug("Wallet analysis retention cleanup deleted raw={}, enriched={}, snapshots={}, insights={}",
                    rawDeleted, enrichedDeleted, snapshotDeleted, insightDeleted);
        }
    }

    private void record(String table, int deleted) {
        if (meterRegistry == null || deleted <= 0) {
            return;
        }
        Counter.builder("wallet_analysis_retention_deleted_rows")
                .tag("table", table)
                .register(meterRegistry)
                .increment(deleted);
    }
}
