package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.WalletTxRaw;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletTxRawRepository extends JpaRepository<WalletTxRaw, Long> {

    Optional<WalletTxRaw> findByWalletIdAndTxHashAndLogIndex(Long walletId, String txHash, long logIndex);

    List<WalletTxRaw> findByWalletIdAndTxAtBetweenOrderByTxAtAsc(Long walletId, Instant from, Instant to);

    void deleteByWalletId(Long walletId);

    @Modifying
    @Query("delete from WalletTxRaw r where r.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") Instant threshold);
}
