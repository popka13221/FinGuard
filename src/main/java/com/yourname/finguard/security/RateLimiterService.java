package com.yourname.finguard.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private static final class Bucket {
        long windowStartMs;
        int count;
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int limit;
    private final long windowMs;

    public RateLimiterService(
            @Value("${app.security.rate-limit.auth.limit:30}") int limit,
            @Value("${app.security.rate-limit.auth.window-ms:60000}") long windowMs
    ) {
        this.limit = limit;
        this.windowMs = windowMs;
    }

    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(true);
        buckets.compute(key, (k, bucket) -> {
            if (bucket == null || now - bucket.windowStartMs >= windowMs) {
                Bucket fresh = new Bucket();
                fresh.windowStartMs = now;
                fresh.count = 1;
                return fresh;
            }
            if (bucket.count >= limit) {
                allowed.set(false);
                return bucket;
            }
            bucket.count += 1;
            return bucket;
        });
        return allowed.get();
    }

    public void reset() {
        buckets.clear();
    }
}
