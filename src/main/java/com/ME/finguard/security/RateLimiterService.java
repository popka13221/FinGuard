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
        long windowMs;
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int limit;
    private final long windowMs;
    private final int maxEntries;

    public RateLimiterService(
            @Value("${app.security.rate-limit.auth.limit:30}") int limit,
            @Value("${app.security.rate-limit.auth.window-ms:60000}") long windowMs,
            @Value("${app.security.rate-limit.max-entries:10000}") int maxEntries
    ) {
        this.limit = limit;
        this.windowMs = windowMs;
        this.maxEntries = maxEntries <= 0 ? 1000 : maxEntries;
    }

    public boolean allow(String key) {
        return allow(key, limit, windowMs);
    }

    public boolean allow(String key, int customLimit, long customWindowMs) {
        if (customLimit <= 0 || customWindowMs <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(true);
        buckets.compute(key, (k, bucket) -> {
            if (bucket == null || now - bucket.windowStartMs >= bucketWindowMs(bucket, customWindowMs)) {
                Bucket fresh = new Bucket();
                fresh.windowStartMs = now;
                fresh.count = 1;
                fresh.windowMs = customWindowMs;
                return fresh;
            }
            if (bucket.count >= customLimit) {
                allowed.set(false);
                return bucket;
            }
            bucket.count += 1;
            bucket.windowMs = customWindowMs;
            return bucket;
        });
        evictExpired(now);
        evictIfNeeded();
        return allowed.get();
    }

    public void reset() {
        buckets.clear();
    }

    /**
     * Used in tests/diagnostics; not intended for request path logic.
     */
    int getApproximateBucketCount() {
        return buckets.size();
    }

    private long bucketWindowMs(Bucket bucket, long customWindowMs) {
        return bucket == null || bucket.windowMs <= 0 ? customWindowMs : bucket.windowMs;
    }

    private void evictExpired(long now) {
        buckets.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            long window = bucket.windowMs <= 0 ? windowMs : bucket.windowMs;
            return now - bucket.windowStartMs >= window;
        });
    }

    private void evictIfNeeded() {
        int size = buckets.size();
        if (size <= maxEntries) {
            return;
        }
        // remove oldest buckets first
        buckets.entrySet().stream()
                .sorted((a, b) -> Long.compare(a.getValue().windowStartMs, b.getValue().windowStartMs))
                .limit(Math.max(0, size - maxEntries))
                .map(Map.Entry::getKey)
                .forEach(buckets::remove);
    }
}
