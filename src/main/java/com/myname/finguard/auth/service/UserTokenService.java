package com.myname.finguard.auth.service;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.model.UserToken;
import com.myname.finguard.auth.model.UserTokenType;
import com.myname.finguard.auth.repository.UserTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserTokenService {

    private static final Logger log = LoggerFactory.getLogger(UserTokenService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserTokenRepository userTokenRepository;
    private final Duration verifyTtl;
    private final Duration resetTtl;
    private final Duration resetCooldown;
    private final String fixedCode;

    public UserTokenService(UserTokenRepository userTokenRepository,
                            @Value("${app.security.tokens.fixed-code:}") String fixedCode,
                            Environment environment,
                            @Value("${app.security.tokens.verify-ttl-minutes:1440}") long verifyTtlMinutes,
                            @Value("${app.security.tokens.reset-ttl-minutes:60}") long resetTtlMinutes,
                            @Value("${app.security.tokens.reset-cooldown-seconds:60}") long resetCooldownSeconds) {
        this.userTokenRepository = userTokenRepository;
        this.verifyTtl = Duration.ofMinutes(Math.max(verifyTtlMinutes, 1));
        this.resetTtl = Duration.ofMinutes(Math.max(resetTtlMinutes, 1));
        this.resetCooldown = Duration.ofSeconds(resetCooldownSeconds);
        this.fixedCode = fixedCode == null ? "" : fixedCode.trim();
        if (!this.fixedCode.isBlank() && isProdProfile(environment)) {
            throw new IllegalStateException("app.security.tokens.fixed-code must be empty in prod profile");
        }
    }

    @Transactional
    public String issue(User user, UserTokenType type) {
        Duration ttl = type == UserTokenType.VERIFY ? verifyTtl : resetTtl;
        invalidateActive(user, type);
        String tokenValue = generateToken(type);
        String hashed = hashToken(tokenValue);
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
        if (hashed.isEmpty() || type == null) {
            return Optional.empty();
        }
        java.util.List<UserToken> matches = userTokenRepository.findByTokenHashAndTypeAndUsedAtIsNullAndExpiresAtAfter(
                hashed, type, Instant.now()
        );
        if (matches.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(matches.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findAny(String token, UserTokenType type) {
        String hashed = hashToken(token);
        if (hashed.isEmpty() || type == null) {
            return Optional.empty();
        }
        java.util.List<UserToken> matches = userTokenRepository.findByTokenHashAndType(hashed, type);
        if (matches.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(matches.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findAnyForEmail(String email, String token, UserTokenType type) {
        String normalizedEmail = normalizeEmail(email);
        String hashed = hashToken(token);
        if (normalizedEmail.isBlank() || hashed.isEmpty() || type == null) {
            return Optional.empty();
        }
        return userTokenRepository.findFirstByUserEmailIgnoreCaseAndTokenHashAndTypeOrderByCreatedAtDesc(
                normalizedEmail, hashed, type);
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
        if (!fixedCode.isBlank()) {
            return fixedCode;
        }
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
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

    private boolean isProdProfile(Environment environment) {
        if (environment == null) {
            return false;
        }
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
