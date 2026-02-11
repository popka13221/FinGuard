package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.WalletDailySnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletDailySnapshotRepository extends JpaRepository<WalletDailySnapshot, Long> {

    Optional<WalletDailySnapshot> findByWalletIdAndDay(Long walletId, LocalDate day);

    Optional<WalletDailySnapshot> findTopByWalletIdOrderByDayDesc(Long walletId);

    Optional<WalletDailySnapshot> findTopByWalletIdAndDayBeforeOrderByDayDesc(Long walletId, LocalDate day);

    List<WalletDailySnapshot> findByWalletIdAndDayBetweenOrderByDayAsc(Long walletId, LocalDate from, LocalDate to);

    void deleteByWalletId(Long walletId);

    @Modifying
    @Query("delete from WalletDailySnapshot s where s.day < :threshold")
    int deleteByDayBefore(@Param("threshold") LocalDate threshold);
}
