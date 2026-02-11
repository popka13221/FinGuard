package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import com.myname.finguard.crypto.model.CryptoWalletAnalysisJobStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CryptoWalletAnalysisJobRepository extends JpaRepository<CryptoWalletAnalysisJob, Long> {

    Optional<CryptoWalletAnalysisJob> findTopByWalletIdOrderByCreatedAtDesc(Long walletId);

    Optional<CryptoWalletAnalysisJob> findTopByWalletIdAndUserIdOrderByCreatedAtDesc(Long walletId, Long userId);

    List<CryptoWalletAnalysisJob> findTop100ByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            Collection<CryptoWalletAnalysisJobStatus> statuses,
            Instant updatedBefore
    );
}
