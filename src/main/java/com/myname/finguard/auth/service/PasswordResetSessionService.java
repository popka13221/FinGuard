package com.myname.finguard.auth.service;

import com.myname.finguard.auth.model.PasswordResetSession;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.PasswordResetSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PasswordResetSessionService {

    private final PasswordResetSessionRepository repository;
    private final Duration sessionTtl;

    public PasswordResetSessionService(PasswordResetSessionRepository repository,
                                       @Value("${app.security.tokens.reset-session-ttl-minutes:15}") long sessionTtlMinutes) {
        this.repository = repository;
        this.sessionTtl = Duration.ofMinutes(Math.max(sessionTtlMinutes, 1));
    }

    @Transactional
    public CreatedSession create(User user, Instant maxExpiry, String ip, String userAgent) {
        if (user == null) {
            throw new IllegalArgumentException("User required for reset session");
        }
        Instant now = Instant.now();
        repository.deleteExpired(now);
        repository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();
        PasswordResetSession session = new PasswordResetSession();
        session.setUser(user);
        session.setTokenHash(hashToken(rawToken));
        session.setIpHash(hash(ip));
        session.setUserAgentHash(hash(userAgent));
        Instant expiry = now.plus(sessionTtl);
        if (maxExpiry != null && maxExpiry.isBefore(expiry)) {
            expiry = maxExpiry;
        }
        session.setExpiresAt(expiry);
        PasswordResetSession saved = repository.save(session);
        return new CreatedSession(saved, rawToken);
    }

    @Transactional(readOnly = true)
    public Optional<PasswordResetSession> findActive(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return Optional.empty();
        }
        Instant now = Instant.now();
        String tokenHash = hashToken(rawToken);
        return repository.findByTokenHash(tokenHash)
                .filter(s -> s.getConsumedAt() == null && s.getExpiresAt() != null && s.getExpiresAt().isAfter(now));
    }

    public record CreatedSession(PasswordResetSession session, String rawToken) {
    }

    @Transactional
    public void consume(PasswordResetSession session) {
        if (session == null) return;
        session.setConsumedAt(Instant.now());
        repository.save(session);
        repository.deleteExpired(Instant.now());
    }

    @Transactional
    public void invalidateForUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        repository.deleteByUserId(user.getId());
    }

    public Duration getSessionTtl() {
        return sessionTtl;
    }

    public String hash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        if ("::1".equals(normalized)) {
            normalized = "127.0.0.1";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public String hashToken(String value) {
        if (!StringUtils.hasText(value)) {
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

    public boolean matchesContext(PasswordResetSession session, String ip, String userAgent) {
        ContextCheckResult result = evaluateContext(session, ip, userAgent, session == null ? "" : session.getIpHash(), session == null ? "" : session.getUserAgentHash());
        return result.isExactMatch();
    }

    public ContextCheckResult evaluateContext(PasswordResetSession session, String ip, String userAgent, String tokenIpHash, String tokenUserAgentHash) {
        if (session == null) {
            return ContextCheckResult.rejected();
        }
        String expectedIp = session.getIpHash();
        String expectedUa = session.getUserAgentHash();
        String ipHash = hash(ip);
        String uaHash = hash(userAgent);
        boolean ipMatches = !StringUtils.hasText(expectedIp) || expectedIp.equals(ipHash);
        boolean uaMatches = !StringUtils.hasText(expectedUa) || expectedUa.equals(uaHash);
        boolean tokenIpMatches = !StringUtils.hasText(expectedIp) || expectedIp.equals(tokenIpHash);
        boolean tokenUaMatches = !StringUtils.hasText(expectedUa) || expectedUa.equals(tokenUserAgentHash);
        boolean tampered = !tokenIpMatches || !tokenUaMatches;
        boolean shouldReject = tampered || (!ipMatches && !uaMatches);
        return new ContextCheckResult(ipMatches, uaMatches, tampered, shouldReject, ipHash, uaHash);
    }

    public record ContextCheckResult(boolean ipMatches, boolean userAgentMatches, boolean tampered, boolean shouldReject, String requestIpHash, String requestUaHash) {
        public static ContextCheckResult rejected() {
            return new ContextCheckResult(false, false, false, true, "", "");
        }

        public boolean isSoftMismatch() {
            return !shouldReject && (!ipMatches || !userAgentMatches);
        }

        public boolean isExactMatch() {
            return ipMatches && userAgentMatches && !tampered;
        }
    }
}
