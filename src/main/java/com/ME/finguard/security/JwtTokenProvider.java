package com.yourname.finguard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements InitializingBean {

    private static final String DEFAULT_DEV_SECRET = "Q29kZXhEZW1vMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";

    private final Key signingKey;
    private final long validitySeconds;
    private final long refreshValiditySeconds;
    private final String issuer;
    private final String audience;
    private final String secretRaw;
    private final boolean requireEnvSecret;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-seconds:3600}") long validitySeconds,
            @Value("${app.security.jwt.refresh-expiration-seconds:604800}") long refreshValiditySeconds,
            @Value("${app.security.jwt.issuer:finguard}") String issuer,
            @Value("${app.security.jwt.audience:finguard-app}") String audience,
            @Value("${app.security.jwt.require-env-secret:true}") boolean requireEnvSecret
    ) {
        this.secretRaw = secret;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.validitySeconds = validitySeconds;
        this.refreshValiditySeconds = refreshValiditySeconds;
        this.issuer = issuer;
        this.audience = audience;
        this.requireEnvSecret = requireEnvSecret;
    }

    @Override
    public void afterPropertiesSet() {
        if (secretRaw == null || secretRaw.isBlank()) {
            throw new IllegalStateException("JWT secret must be set");
        }
        byte[] bytes = Decoders.BASE64.decode(secretRaw);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret is too short; must be at least 256 bits (32 bytes) before Base64 encoding");
        }
        if (requireEnvSecret && DEFAULT_DEV_SECRET.equals(secretRaw)) {
            throw new IllegalStateException("JWT secret must be overridden via environment variable in this environment");
        }
    }

    public long getValiditySeconds() {
        return validitySeconds;
    }

    public long getRefreshValiditySeconds() {
        return refreshValiditySeconds;
    }

    public String generateAccessToken(Long userId, String email) {
        return buildToken(userId, email, validitySeconds, "access", null, null);
    }

    public String generateRefreshToken(Long userId, String email) {
        return buildToken(userId, email, refreshValiditySeconds, "refresh", null, null);
    }

    public String generateResetSessionToken(Long userId, String email, String jti, long ttlSeconds, String ipHash, String userAgentHash) {
        Map<String, Object> claims = Map.of(
                "rsh", ipHash == null ? "" : ipHash,
                "uah", userAgentHash == null ? "" : userAgentHash
        );
        return buildToken(userId, email, ttlSeconds, "reset_session", jti, claims);
    }

    public ResetSessionClaims parseResetSessionToken(String token) {
        Claims claims = parseClaims(token);
        Object typ = claims.get("typ");
        if (typ == null || !"reset_session".equals(typ.toString())) {
            throw new JwtException("Invalid token type");
        }
        Long userId = getUserIdFromClaims(claims);
        String email = claims.getSubject();
        String jti = claims.getId();
        String ipHash = toStringClaim(claims.get("rsh"));
        String uaHash = toStringClaim(claims.get("uah"));
        return new ResetSessionClaims(userId, email, jti, ipHash, uaHash, claims.getExpiration().toInstant());
    }

    private String buildToken(Long userId, String email, long ttlSeconds, String type, String jti, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ttlSeconds);
        var builder = Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setAudience(audience)
                .setId(jti == null || jti.isBlank() ? UUID.randomUUID().toString() : jti)
                .claim("uid", userId)
                .claim("typ", type)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry));
        if (extraClaims != null) {
            extraClaims.forEach((k, v) -> {
                if (v != null) {
                    builder.claim(k, v);
                }
            });
        }
        return builder.signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String getType(String token) {
        Object typ = parseClaims(token).get("typ");
        return typ == null ? "" : typ.toString();
    }

    public Long getUserId(String token) {
        return getUserIdFromClaims(parseClaims(token));
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    public Date getExpiry(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!issuer.equals(claims.getIssuer())) return false;
            if (!audience.equals(claims.getAudience())) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw e;
        }
    }

    private Long getUserIdFromClaims(Claims claims) {
        Object uid = claims.get("uid");
        if (uid instanceof Number n) {
            return n.longValue();
        }
        if (uid != null) {
            try {
                return Long.parseLong(uid.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String toStringClaim(Object value) {
        return value == null ? "" : value.toString();
    }

    public record ResetSessionClaims(Long userId, String email, String jti, String ipHash, String userAgentHash, Instant expiresAt) {
    }
}
