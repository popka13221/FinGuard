package com.yourname.finguard.security;

import com.yourname.finguard.security.model.OtpCode;
import com.yourname.finguard.security.repository.OtpCodeRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeRepository otpCodeRepository;
    private final long ttlSeconds;
    private final String devCode;
    private final int maxEntries;
    private final int maxAttempts;

    public OtpService(OtpCodeRepository otpCodeRepository,
                      @Value("${app.security.otp.ttl-seconds:300}") long ttlSeconds,
                      @Value("${app.security.otp.dev-code:}") String devCode,
                      @Value("${app.security.otp.max-entries:10000}") int maxEntries,
                      @Value("${app.security.otp.max-attempts:5}") int maxAttempts) {
        this.otpCodeRepository = otpCodeRepository;
        this.ttlSeconds = Math.max(30, ttlSeconds);
        this.devCode = devCode == null ? "" : devCode.trim();
        this.maxEntries = Math.max(maxEntries, 1000);
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @Transactional
    public IssuedOtp issue(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email required for OTP");
        }
        purgeExpired();
        evictIfNeeded();
        String code = generateCode();
        OtpCode entry = otpCodeRepository.findByEmail(normalize(email)).orElseGet(OtpCode::new);
        entry.setEmail(normalize(email));
        entry.setCodeHash(hash(code));
        entry.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        entry.setAttempts(0);
        entry.setUpdatedAt(Instant.now());
        otpCodeRepository.save(entry);
        return new IssuedOtp(code, entry.getExpiresAt(), true);
    }

    @Transactional(readOnly = true)
    public IssuedOtp getActive(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email required for OTP");
        }
        OtpCode existing = otpCodeRepository.findByEmail(normalize(email)).orElse(null);
        Instant now = Instant.now();
        if (existing != null && existing.getExpiresAt() != null && existing.getExpiresAt().isAfter(now)) {
            return new IssuedOtp("", existing.getExpiresAt(), false);
        }
        return null;
    }

    @Transactional
    public boolean verify(String email, String code) {
        purgeExpired();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            return false;
        }
        // Allow dev-code override for local/testing flows
        if (StringUtils.hasText(devCode) && devCode.equals(code.trim())) {
            OtpCode existing = otpCodeRepository.findByEmail(normalize(email)).orElse(null);
            if (existing != null) {
                otpCodeRepository.delete(existing);
            }
            log.warn("OTP dev-code used for email={}", normalize(email));
            return true;
        }
        OtpCode entry = otpCodeRepository.findByEmail(normalize(email)).orElse(null);
        if (entry == null || entry.getExpiresAt() == null || entry.getExpiresAt().isBefore(Instant.now())) {
            if (entry != null) {
                otpCodeRepository.delete(entry);
            }
            return false;
        }
        boolean ok = hash(code.trim()).equals(entry.getCodeHash());
        if (ok) {
            otpCodeRepository.delete(entry);
            return true;
        }
        entry.setAttempts(entry.getAttempts() + 1);
        entry.setUpdatedAt(Instant.now());
        if (entry.getAttempts() >= maxAttempts) {
            otpCodeRepository.delete(entry);
        } else {
            otpCodeRepository.save(entry);
        }
        return false;
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
        otpCodeRepository.deleteExpired(Instant.now());
    }

    private void evictIfNeeded() {
        long count = otpCodeRepository.count();
        if (count < maxEntries) {
            return;
        }
        int toRemove = (int) Math.max(0, count - maxEntries + 1);
        for (OtpCode code : otpCodeRepository.findTop50ByOrderByCreatedAtAsc()) {
            if (toRemove-- <= 0) break;
            otpCodeRepository.delete(code);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record IssuedOtp(String code, Instant expiresAt, boolean newlyCreated) {
    }
}
