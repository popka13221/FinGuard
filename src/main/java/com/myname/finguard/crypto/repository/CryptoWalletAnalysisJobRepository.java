package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.CryptoWalletAnalysisJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CryptoWalletAnalysisJobRepository extends JpaRepository<CryptoWalletAnalysisJob, Long> {

    Optional<CryptoWalletAnalysisJob> findTopByWalletIdOrderByCreatedAtDesc(Long walletId);

    Optional<CryptoWalletAnalysisJob> findTopByWalletIdAndUserIdOrderByCreatedAtDesc(Long walletId, Long userId);
}
