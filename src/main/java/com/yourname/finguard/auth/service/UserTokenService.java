package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.model.UserToken;
import com.yourname.finguard.auth.model.UserTokenType;
import com.yourname.finguard.auth.repository.UserTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTokenService {

    private final UserTokenRepository userTokenRepository;
    private final Duration verifyTtl;
    private final Duration resetTtl;
    private final String resetDevCode;

    public UserTokenService(UserTokenRepository userTokenRepository,
                            @Value("${app.security.tokens.verify-ttl-minutes:1440}") long verifyTtlMinutes,
                            @Value("${app.security.tokens.reset-ttl-minutes:60}") long resetTtlMinutes,
                            @Value("${app.security.tokens.reset-dev-code:123456}") String resetDevCode) {
        this.userTokenRepository = userTokenRepository;
        this.verifyTtl = Duration.ofMinutes(verifyTtlMinutes);
        this.resetTtl = Duration.ofMinutes(resetTtlMinutes);
        this.resetDevCode = resetDevCode == null ? "" : resetDevCode.trim();
    }

    @Transactional
    public String issue(User user, UserTokenType type) {
        Duration ttl = type == UserTokenType.VERIFY ? verifyTtl : resetTtl;
        String tokenValue = generateToken(type);
        if (type == UserTokenType.RESET && !resetDevCode.isEmpty()) {
            userTokenRepository.deleteByType(UserTokenType.RESET);
        }
        UserToken token = new UserToken();
        token.setUser(user);
        token.setToken(tokenValue);
        token.setType(type);
        token.setExpiresAt(Instant.now().plus(ttl));
        userTokenRepository.save(token);
        return token.getToken();
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findValid(String token, UserTokenType type) {
        return userTokenRepository.findByTokenAndTypeAndUsedAtIsNullAndExpiresAtAfter(
                token, type, Instant.now()
        );
    }

    @Transactional
    public void markUsed(UserToken token) {
        token.setUsedAt(Instant.now());
        userTokenRepository.save(token);
    }

    public Duration getResetTtl() {
        return resetTtl;
    }

    private String generateToken(UserTokenType type) {
        if (type == UserTokenType.RESET && !resetDevCode.isEmpty()) {
            return resetDevCode;
        }
        return UUID.randomUUID().toString();
    }
}
