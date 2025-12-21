package com.myname.finguard.auth.service;

import com.myname.finguard.auth.model.PendingRegistration;
import com.myname.finguard.auth.repository.PendingRegistrationRepository;
import com.myname.finguard.common.model.Role;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PendingRegistrationService {

    private final PendingRegistrationRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Duration verifyTtl;

    public PendingRegistrationService(PendingRegistrationRepository repository,
                                      PasswordEncoder passwordEncoder,
                                      @Value("${app.security.tokens.verify-ttl-minutes:1440}") long verifyTtlMinutes) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.verifyTtl = Duration.ofMinutes(Math.max(verifyTtlMinutes, 1));
    }

    @Transactional
    public IssuedToken createOrUpdate(String email, String rawPassword, String fullName, String baseCurrency, Role role, String rawToken) {
        purgeExpired();
        PendingRegistration pending = repository.findByEmail(email).orElseGet(PendingRegistration::new);
        pending.setEmail(email);
        pending.setPasswordHash(passwordEncoder.encode(rawPassword));
        pending.setFullName(fullName);
        pending.setBaseCurrency(baseCurrency);
        pending.setRole(role == null ? Role.USER : role);
        pending.setVerifyTokenHash(hash(rawToken));
        Instant expiresAt = Instant.now().plus(verifyTtl);
        pending.setVerifyExpiresAt(expiresAt);
        pending.setUpdatedAt(Instant.now());
        if (pending.getCreatedAt() == null) {
            pending.setCreatedAt(Instant.now());
        }
        PendingRegistration saved = repository.save(pending);
        return new IssuedToken(saved, expiresAt);
    }

    @Transactional
    public Optional<PendingRegistration> findValid(String email, String rawToken) {
        purgeExpired();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(rawToken)) {
            return Optional.empty();
        }
        return repository.findByEmail(email)
                .filter(p -> p.getVerifyExpiresAt() != null && p.getVerifyExpiresAt().isAfter(Instant.now()))
                .filter(p -> hash(rawToken).equals(p.getVerifyTokenHash()));
    }

    @Transactional
    public void delete(PendingRegistration pending) {
        if (pending != null && pending.getId() != null) {
            repository.deleteById(pending.getId());
        }
    }

    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(Instant.now());
    }

    public Duration getVerifyTtl() {
        return verifyTtl;
    }

    private String hash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record IssuedToken(PendingRegistration pending, Instant expiresAt) {
    }
}
