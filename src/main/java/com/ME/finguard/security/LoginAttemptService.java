package com.yourname.finguard.security;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LoginAttemptService {

    private static final class AttemptInfo {
        int attempts;
        Instant lockUntil;
    }

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockDuration;

    public LoginAttemptService(
            @Value("${app.security.lockout.max-attempts:5}") int maxAttempts,
            @Value("${app.security.lockout.lock-minutes:15}") long lockMinutes
    ) {
        this.maxAttempts = maxAttempts;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
    }

    @PostConstruct
    void warnInMemory() {
        log.warn("LoginAttemptService is using in-memory storage; lockout state resets on application restart.");
    }

    public boolean isLocked(String email) {
        String key = normalize(email);
        AttemptInfo info = attempts.get(key);
        if (info == null || info.lockUntil == null) {
            return false;
        }
        if (Instant.now().isBefore(info.lockUntil)) {
            return true;
        }
        attempts.remove(key);
        return false;
    }

    public long lockRemainingSeconds(String email) {
        String key = normalize(email);
        AttemptInfo info = attempts.get(key);
        if (info == null || info.lockUntil == null) {
            return 0;
        }
        long remaining = Duration.between(Instant.now(), info.lockUntil).getSeconds();
        return Math.max(remaining, 0);
    }

    public void recordFailure(String email) {
        String key = normalize(email);
        AttemptInfo info = attempts.computeIfAbsent(key, k -> new AttemptInfo());
        info.attempts++;
        if (info.attempts >= maxAttempts) {
            info.lockUntil = Instant.now().plus(lockDuration);
            info.attempts = 0;
        }
    }

    public void recordSuccess(String email) {
        attempts.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public void reset() {
        attempts.clear();
    }
}
