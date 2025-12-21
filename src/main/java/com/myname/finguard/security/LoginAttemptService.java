package com.myname.finguard.security;

import com.myname.finguard.security.model.LoginAttempt;
import com.myname.finguard.security.repository.LoginAttemptRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final int maxAttempts;
    private final Duration lockDuration;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository,
            @Value("${app.security.lockout.max-attempts:5}") int maxAttempts,
            @Value("${app.security.lockout.lock-minutes:15}") long lockMinutes) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.maxAttempts = maxAttempts;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
    }

    @Transactional(readOnly = true)
    public boolean isLocked(String email) {
        String key = normalize(email);
        Optional<LoginAttempt> info = loginAttemptRepository.findByEmail(key);
        if (info.isEmpty() || info.get().getLockUntil() == null) {
            return false;
        }
        Instant lockUntil = info.get().getLockUntil();
        if (Instant.now().isBefore(lockUntil)) {
            return true;
        }
        loginAttemptRepository.deleteById(key);
        return false;
    }

    @Transactional(readOnly = true)
    public long lockRemainingSeconds(String email) {
        String key = normalize(email);
        Optional<LoginAttempt> info = loginAttemptRepository.findByEmail(key);
        if (info.isEmpty() || info.get().getLockUntil() == null) {
            return 0;
        }
        long remaining = Duration.between(Instant.now(), info.get().getLockUntil()).getSeconds();
        return Math.max(remaining, 0);
    }

    @Transactional
    public void recordFailure(String email) {
        String key = normalize(email);
        LoginAttempt info = loginAttemptRepository.findByEmail(key).orElseGet(() -> {
            LoginAttempt attempt = new LoginAttempt();
            attempt.setEmail(key);
            attempt.setAttempts(0);
            return attempt;
        });
        info.setAttempts(info.getAttempts() + 1);
        if (info.getAttempts() >= maxAttempts) {
            info.setLockUntil(Instant.now().plus(lockDuration));
            info.setAttempts(0);
        }
        info.setUpdatedAt(Instant.now());
        loginAttemptRepository.save(info);
    }

    @Transactional
    public void recordSuccess(String email) {
        loginAttemptRepository.deleteById(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    @Transactional
    public void reset() {
        loginAttemptRepository.deleteAll();
    }
}
