package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.model.PasswordResetSession;
import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.repository.PasswordResetSessionRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
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
    public PasswordResetSession create(User user, Instant maxExpiry, String ip, String userAgent) {
        if (user == null) {
            throw new IllegalArgumentException("User required for reset session");
        }
        Instant now = Instant.now();
        repository.deleteExpired(now);
        repository.deleteByUserId(user.getId());

        PasswordResetSession session = new PasswordResetSession();
        session.setUser(user);
        session.setJti(UUID.randomUUID().toString());
        session.setIpHash(hash(ip));
        session.setUserAgentHash(hash(userAgent));
        Instant expiry = now.plus(sessionTtl);
        if (maxExpiry != null && maxExpiry.isBefore(expiry)) {
            expiry = maxExpiry;
        }
        session.setExpiresAt(expiry);
        return repository.save(session);
    }

    @Transactional(readOnly = true)
    public Optional<PasswordResetSession> findActive(String jti) {
        if (!StringUtils.hasText(jti)) {
            return Optional.empty();
        }
        Instant now = Instant.now();
        return repository.findByJti(jti)
                .filter(s -> s.getConsumedAt() == null && s.getExpiresAt() != null && s.getExpiresAt().isAfter(now));
    }

    @Transactional
    public void consume(PasswordResetSession session) {
        if (session == null) return;
        session.setConsumedAt(Instant.now());
        repository.save(session);
        repository.deleteExpired(Instant.now());
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
        return DigestUtils.md5DigestAsHex(normalized.getBytes(StandardCharsets.UTF_8));
    }

    public boolean matchesContext(PasswordResetSession session, String ip, String userAgent) {
        if (session == null) {
            return false;
        }
        String expectedIp = session.getIpHash();
        String expectedUa = session.getUserAgentHash();
        if (StringUtils.hasText(expectedIp) && !expectedIp.equals(hash(ip))) {
            return false;
        }
        if (StringUtils.hasText(expectedUa) && !expectedUa.equals(hash(userAgent))) {
            return false;
        }
        return true;
    }
}
