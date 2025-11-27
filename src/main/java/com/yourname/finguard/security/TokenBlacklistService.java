package com.yourname.finguard.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private static final class Revoked {
        Instant expiresAt;
    }

    private final Map<String, Revoked> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank() || expiresAt == null) {
            return;
        }
        Revoked r = new Revoked();
        r.expiresAt = expiresAt;
        revoked.put(jti, r);
    }

    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Revoked r = revoked.get(jti);
        if (r == null) {
            return false;
        }
        if (Instant.now().isAfter(r.expiresAt)) {
            revoked.remove(jti);
            return false;
        }
        return true;
    }

    public void clearAll() {
        revoked.clear();
    }
}
