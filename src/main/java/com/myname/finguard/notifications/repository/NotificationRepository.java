package com.myname.finguard.notifications.repository;

import com.myname.finguard.notifications.model.Notification;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndReadAtIsNull(Long userId);

    boolean existsByRuleIdAndPeriodStart(Long ruleId, LocalDate periodStart);

    long deleteByRuleId(Long ruleId);

    @Modifying
    @Query("update Notification n set n.readAt = :readAt where n.user.id = :userId and n.id in :ids and n.readAt is null")
    int updateReadAtForIds(@Param("userId") Long userId, @Param("ids") List<Long> ids, @Param("readAt") Instant readAt);

    @Modifying
    @Query("update Notification n set n.readAt = :readAt where n.user.id = :userId and n.readAt is null")
    int markAllRead(@Param("userId") Long userId, @Param("readAt") Instant readAt);
}
