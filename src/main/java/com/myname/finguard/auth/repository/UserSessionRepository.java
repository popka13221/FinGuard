package com.myname.finguard.auth.repository;

import com.myname.finguard.auth.model.UserSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByJti(String jti);

    @Modifying
    @Query("delete from UserSession s where s.expiresAt < ?1")
    @Transactional
    void deleteExpired(Instant now);

    List<UserSession> findTop10ByUserIdOrderByCreatedAtAsc(Long userId);

    List<UserSession> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
