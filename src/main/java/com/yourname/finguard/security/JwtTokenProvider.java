package com.yourname.finguard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final long validitySeconds;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-seconds:86400}") long validitySeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.validitySeconds = validitySeconds;
    }

    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(validitySeconds);
        return Jwts.builder()
                .setSubject(email)
                .claim("uid", userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
