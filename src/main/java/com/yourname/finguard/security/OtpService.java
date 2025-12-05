package com.yourname.finguard.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final class OtpEntry {
        String code;
        Instant expiresAt;
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final long ttlSeconds;
    private final String devCode;
    private final int maxEntries;

    public OtpService(@Value("${app.security.otp.ttl-seconds:300}") long ttlSeconds,
                      @Value("${app.security.otp.dev-code:}") String devCode,
                      @Value("${app.security.otp.max-entries:10000}") int maxEntries) {
        this.ttlSeconds = Math.max(30, ttlSeconds);
        this.devCode = devCode == null ? "" : devCode.trim();
        this.maxEntries = Math.max(maxEntries, 1000);
    }

    public IssuedOtp issue(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email required for OTP");
        }
        purgeExpired();
        if (store.size() >= maxEntries) {
            evictOldest();
        }
        String code = generateCode();
        OtpEntry entry = new OtpEntry();
        entry.code = code;
        entry.expiresAt = Instant.now().plusSeconds(ttlSeconds);
        store.put(normalize(email), entry);
        return new IssuedOtp(code, entry.expiresAt);
    }

    public boolean verify(String email, String code) {
        purgeExpired();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            return false;
        }
        OtpEntry entry = store.get(normalize(email));
        if (entry == null || entry.expiresAt == null || entry.expiresAt.isBefore(Instant.now())) {
            store.remove(normalize(email));
            return false;
        }
        boolean ok = code.trim().equals(entry.code);
        if (ok) {
            store.remove(normalize(email));
        }
        return ok;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    private String generateCode() {
        if (StringUtils.hasText(devCode)) {
            return devCode;
        }
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> e.getValue().expiresAt == null || e.getValue().expiresAt.isBefore(now));
    }

    private void evictOldest() {
        store.entrySet().stream()
                .sorted((a, b) -> {
                    Instant ea = a.getValue().expiresAt == null ? Instant.EPOCH : a.getValue().expiresAt;
                    Instant eb = b.getValue().expiresAt == null ? Instant.EPOCH : b.getValue().expiresAt;
                    return ea.compareTo(eb);
                })
                .limit(Math.max(0, store.size() - maxEntries + 1))
                .map(Map.Entry::getKey)
                .forEach(store::remove);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    public record IssuedOtp(String code, Instant expiresAt) {
    }
}
