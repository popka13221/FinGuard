package com.yourname.finguard.auth.repository;

import com.yourname.finguard.auth.model.UserToken;
import com.yourname.finguard.auth.model.UserTokenType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenAndTypeAndUsedAtIsNullAndExpiresAtAfter(String token, UserTokenType type, Instant now);

    void deleteByType(UserTokenType type);
    void deleteByTokenAndType(String token, UserTokenType type);
}
