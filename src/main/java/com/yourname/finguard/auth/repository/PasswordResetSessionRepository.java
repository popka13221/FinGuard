package com.yourname.finguard.auth.repository;

import com.yourname.finguard.auth.model.PasswordResetSession;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetSessionRepository extends JpaRepository<PasswordResetSession, Long> {

    Optional<PasswordResetSession> findByJti(String jti);

    @Modifying
    @Query("delete from PasswordResetSession s where s.expiresAt < ?1")
    void deleteExpired(Instant now);

    @Modifying
    @Query("delete from PasswordResetSession s where s.user.id = ?1")
    void deleteByUserId(Long userId);
}
