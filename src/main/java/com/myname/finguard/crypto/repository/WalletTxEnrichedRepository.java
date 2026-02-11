package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.WalletTxEnriched;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletTxEnrichedRepository extends JpaRepository<WalletTxEnriched, Long> {

    Optional<WalletTxEnriched> findByWalletIdAndTxHashAndLogIndex(Long walletId, String txHash, long logIndex);

    List<WalletTxEnriched> findByWalletIdAndTxAtBetweenOrderByTxAtAsc(Long walletId, Instant from, Instant to);

    List<WalletTxEnriched> findByWalletIdOrderByTxAtDesc(Long walletId);

    void deleteByWalletId(Long walletId);

    @Modifying
    @Query("delete from WalletTxEnriched e where e.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") Instant threshold);
}
