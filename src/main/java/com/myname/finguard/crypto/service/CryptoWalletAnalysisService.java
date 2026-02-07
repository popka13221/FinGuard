package com.myname.finguard.crypto.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.crypto.dto.CryptoWalletAnalysisStatusResponse;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJobStatus;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisStage;
import com.myname.finguard.crypto.repository.CryptoWalletAnalysisJobRepository;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CryptoWalletAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CryptoWalletAnalysisService.class);

    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoWalletAnalysisJobRepository jobRepository;
    private final long simulatedDelayMs;

    public CryptoWalletAnalysisService(
            CryptoWalletRepository cryptoWalletRepository,
            CryptoWalletAnalysisJobRepository jobRepository,
            @Value("${app.crypto.analysis.simulated-delay-ms:300}") long simulatedDelayMs
    ) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.jobRepository = jobRepository;
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
            // Start bootstrap lazily for wallets created before analysis jobs existed.
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

    private boolean isTerminal(CryptoWalletAnalysisJobStatus status) {
        return status == CryptoWalletAnalysisJobStatus.DONE || status == CryptoWalletAnalysisJobStatus.FAILED;
    }

    private ApiException walletNotFound() {
        return new ApiException(ErrorCodes.BAD_REQUEST, "Wallet not found", HttpStatus.BAD_REQUEST);
    }
}
