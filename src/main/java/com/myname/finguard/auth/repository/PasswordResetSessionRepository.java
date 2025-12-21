package com.myname.finguard.auth.repository;

import com.myname.finguard.auth.model.PasswordResetSession;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetSessionRepository extends JpaRepository<PasswordResetSession, Long> {
    Optional<PasswordResetSession> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from PasswordResetSession s where s.expiresAt < ?1")
    @Transactional
    void deleteExpired(Instant now);

    @Modifying
    @Query("delete from PasswordResetSession s where s.user.id = ?1")
    void deleteByUserId(Long userId);
}
