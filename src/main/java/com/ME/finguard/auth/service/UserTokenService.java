package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.model.UserToken;
import com.yourname.finguard.auth.model.UserTokenType;
import com.yourname.finguard.auth.repository.UserTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserTokenService {

    private static final Logger log = LoggerFactory.getLogger(UserTokenService.class);
    // TODO remove fixed code before production; only for temporary simplified flow
    private static final String FIXED_CODE = "654321";
    private final UserTokenRepository userTokenRepository;
    private final Duration verifyTtl;
    private final Duration resetTtl;
    private final Duration resetCooldown;

    public UserTokenService(UserTokenRepository userTokenRepository,
                            @Value("${app.security.tokens.verify-ttl-minutes:1440}") long verifyTtlMinutes,
                            @Value("${app.security.tokens.reset-ttl-minutes:60}") long resetTtlMinutes,
                            @Value("${app.security.tokens.reset-cooldown-seconds:60}") long resetCooldownSeconds) {
        this.userTokenRepository = userTokenRepository;
        this.verifyTtl = Duration.ofMinutes(Math.max(verifyTtlMinutes, 1));
        this.resetTtl = Duration.ofMinutes(Math.max(resetTtlMinutes, 1));
        this.resetCooldown = Duration.ofSeconds(resetCooldownSeconds);
    }

    @Transactional
    public String issue(User user, UserTokenType type) {
        Duration ttl = type == UserTokenType.VERIFY ? verifyTtl : resetTtl;
        String tokenValue = generateToken(type);
        String hashed = hashToken(tokenValue);
        // уникальный фиксированный код требует очистки предыдущих записей
        userTokenRepository.deleteByTokenHash(hashed);
        userTokenRepository.flush();
        UserToken token = new UserToken();
        token.setUser(user);
        token.setTokenHash(hashed);
        token.setType(type);
        token.setExpiresAt(Instant.now().plus(ttl));
        userTokenRepository.save(token);
        if (type == UserTokenType.RESET) {
            log.debug("Issued {} token for user={} exp={}", type, user.getEmail(), token.getExpiresAt());
        }
        return tokenValue;
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findValid(String token, UserTokenType type) {
        String hashed = hashToken(token);
        if (hashed.isEmpty()) {
            return Optional.empty();
        }
        return userTokenRepository.findByTokenHashAndTypeAndUsedAtIsNullAndExpiresAtAfter(
                hashed, type, Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findAny(String token, UserTokenType type) {
        String hashed = hashToken(token);
        if (hashed.isEmpty()) {
            return Optional.empty();
        }
        return userTokenRepository.findByTokenHashAndType(hashed, type);
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findAnyForEmail(String email, String token, UserTokenType type) {
        String normalizedEmail = normalizeEmail(email);
        return findAny(token, type)
                .filter(t -> t.getUser() != null && normalizedEmail.equals(normalizeEmail(t.getUser().getEmail())));
    }

    @Transactional
    public void markUsed(UserToken token) {
        token.setUsedAt(Instant.now());
        userTokenRepository.save(token);
    }

    public Duration getResetTtl() {
        return resetTtl;
    }

    public Duration getVerifyTtl() {
        return verifyTtl;
    }

    public Duration getResetCooldown() {
        return resetCooldown;
    }

    @Transactional
    public void invalidateActive(User user, UserTokenType type) {
        if (user == null || type == null) {
            return;
        }
        Instant now = Instant.now();
        userTokenRepository.findByUserAndTypeAndUsedAtIsNullAndExpiresAtAfter(user, type, now)
                .forEach(t -> {
                    t.setUsedAt(now);
                    userTokenRepository.save(t);
                });
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findLatestActiveReset(User user) {
        return userTokenRepository.findFirstByUserAndTypeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                user, UserTokenType.RESET, Instant.now());
    }

    private String generateToken(UserTokenType type) {
        return FIXED_CODE;
    }

    public String generateVerifyCode() {
        return generateToken(UserTokenType.VERIFY);
    }

    private String hashToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
