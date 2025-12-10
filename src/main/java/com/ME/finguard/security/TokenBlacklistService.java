package com.yourname.finguard.security;

import com.yourname.finguard.security.model.RevokedToken;
import com.yourname.finguard.security.repository.RevokedTokenRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenBlacklistService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank() || expiresAt == null) {
            return;
        }
        RevokedToken token = new RevokedToken();
        token.setJti(jti);
        token.setExpiresAt(expiresAt);
        revokedTokenRepository.save(token);
        revokedTokenRepository.deleteExpired(Instant.now());
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        revokedTokenRepository.deleteExpired(Instant.now());
        return revokedTokenRepository.existsByJtiAndExpiresAtAfter(jti, Instant.now());
    }

    @Transactional
    public void clearExpired() {
        revokedTokenRepository.deleteExpired(Instant.now());
    }
}
