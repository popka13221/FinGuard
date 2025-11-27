package com.yourname.finguard.auth.repository;

import com.yourname.finguard.auth.model.UserSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByJti(String jti);

    @Modifying
    @Query("delete from UserSession s where s.expiresAt < ?1")
    void deleteExpired(Instant now);

    List<UserSession> findTop10ByUserIdOrderByCreatedAtAsc(Long userId);
}
