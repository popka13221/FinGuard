package com.myname.finguard.auth.repository;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.model.UserToken;
import com.myname.finguard.auth.model.UserTokenType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenHashAndTypeAndUsedAtIsNullAndExpiresAtAfter(String tokenHash, UserTokenType type, Instant now);

    Optional<UserToken> findByTokenHashAndType(String tokenHash, UserTokenType type);

    Optional<UserToken> findFirstByUserAndTypeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            User user, UserTokenType type, Instant now);

    java.util.List<UserToken> findByUserAndTypeAndUsedAtIsNullAndExpiresAtAfter(User user, UserTokenType type, Instant now);

    void deleteByTokenHash(String tokenHash);

    void deleteByTokenHashAndType(String tokenHash, UserTokenType type);
}
