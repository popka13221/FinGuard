package com.yourname.finguard.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private static final class AttemptInfo {
        int attempts;
        Instant lockUntil;
    }

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String email) {
        AttemptInfo info = attempts.get(normalize(email));
        if (info == null || info.lockUntil == null) {
            return false;
        }
        if (Instant.now().isBefore(info.lockUntil)) {
            return true;
        }
        attempts.remove(normalize(email));
        return false;
    }

    public void recordFailure(String email) {
        String key = normalize(email);
        AttemptInfo info = attempts.computeIfAbsent(key, k -> new AttemptInfo());
        info.attempts++;
        if (info.attempts >= MAX_ATTEMPTS) {
            info.lockUntil = Instant.now().plusMillis(TimeUnit.MINUTES.toMillis(LOCK_MINUTES));
            info.attempts = 0;
        }
    }

    public void recordSuccess(String email) {
        attempts.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
