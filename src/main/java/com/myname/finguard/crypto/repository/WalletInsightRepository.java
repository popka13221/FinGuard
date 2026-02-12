package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.WalletInsight;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletInsightRepository extends JpaRepository<WalletInsight, Long> {

    List<WalletInsight> findTop200ByWalletIdOrderByAsOfDescIdDesc(Long walletId);

    List<WalletInsight> findTop200ByWalletIdAndInsightTypeOrderByAsOfDescIdDesc(Long walletId, String insightType);

    List<WalletInsight> findTop500ByUserIdOrderByAsOfDescIdDesc(Long userId);

    List<WalletInsight> findTop500ByUserIdAndInsightTypeAndNextEstimatedChargeAtAfterOrderByNextEstimatedChargeAtAsc(
            Long userId,
            String insightType,
            Instant after
    );

    void deleteByWalletId(Long walletId);

    @Modifying
    @Query("delete from WalletInsight i where i.asOf < :threshold")
    int deleteByAsOfBefore(@Param("threshold") Instant threshold);
}
