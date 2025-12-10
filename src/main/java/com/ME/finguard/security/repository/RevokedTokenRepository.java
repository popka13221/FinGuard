package com.yourname.finguard.security.repository;

import com.yourname.finguard.security.model.RevokedToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    Optional<RevokedToken> findByJti(String jti);

    boolean existsByJtiAndExpiresAtAfter(String jti, Instant now);

    @Modifying
    @Query("delete from RevokedToken t where t.expiresAt < ?1")
    void deleteExpired(Instant now);
}
