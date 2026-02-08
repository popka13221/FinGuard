package com.myname.finguard.security;

import com.myname.finguard.security.model.RateLimitBucket;
import com.myname.finguard.security.repository.RateLimitBucketRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RateLimiterService {

    private final RateLimitBucketRepository rateLimitBucketRepository;
    private final Map<String, InMemoryBucket> inMemoryBuckets;
    private final Map<String, Object> keyLocks;
    private final int limit;
    private final long windowMs;
    private final int maxEntries;
    private final long cleanupIntervalMs;
    private final LongSupplier nowMs;
    private final AtomicBoolean cleanupInProgress;
    private final AtomicLong lastDbCleanupMs;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimiterService.class);

    @Autowired
    public RateLimiterService(
            RateLimitBucketRepository rateLimitBucketRepository,
            @Value("${app.security.rate-limit.auth.limit:30}") int limit,
            @Value("${app.security.rate-limit.auth.window-ms:60000}") long windowMs,
            @Value("${app.security.rate-limit.max-entries:10000}") int maxEntries,
            @Value("${app.security.rate-limit.cleanup-interval-ms:15000}") long cleanupIntervalMs
    ) {
        this(rateLimitBucketRepository, limit, windowMs, maxEntries, cleanupIntervalMs, System::currentTimeMillis);
    }

    // For unit tests without Spring context
    RateLimiterService(int limit, long windowMs, int maxEntries) {
        this(null, limit, windowMs, maxEntries, 0L, System::currentTimeMillis);
    }

    // For unit tests/diagnostics with controlled clock
    RateLimiterService(int limit, long windowMs, int maxEntries, LongSupplier nowMs) {
        this(null, limit, windowMs, maxEntries, 0L, nowMs);
    }

    // For unit tests verifying repository-backed behavior
    RateLimiterService(RateLimitBucketRepository rateLimitBucketRepository, int limit, long windowMs, int maxEntries, LongSupplier nowMs) {
        this(rateLimitBucketRepository, limit, windowMs, maxEntries, 0L, nowMs);
    }

    // For unit tests verifying repository-backed behavior with custom cleanup
    RateLimiterService(
            RateLimitBucketRepository rateLimitBucketRepository,
            int limit,
            long windowMs,
            int maxEntries,
            long cleanupIntervalMs,
            LongSupplier nowMs
    ) {
        this.rateLimitBucketRepository = rateLimitBucketRepository;
        this.inMemoryBuckets = rateLimitBucketRepository == null ? new ConcurrentHashMap<>() : null;
        this.keyLocks = rateLimitBucketRepository == null ? null : new ConcurrentHashMap<>();
        this.limit = limit;
        this.windowMs = windowMs;
        this.maxEntries = maxEntries <= 0 ? 1000 : maxEntries;
        this.cleanupIntervalMs = cleanupIntervalMs < 0 ? 0 : cleanupIntervalMs;
        this.nowMs = nowMs == null ? System::currentTimeMillis : nowMs;
        this.cleanupInProgress = rateLimitBucketRepository == null ? null : new AtomicBoolean(false);
        this.lastDbCleanupMs = rateLimitBucketRepository == null ? null : new AtomicLong(0);
    }

    public boolean allow(String key) {
        return allow(key, limit, windowMs);
    }

    public boolean allow(String key, int customLimit, long customWindowMs) {
        return check(key, customLimit, customWindowMs).allowed();
    }

    public Result check(String key) {
        return check(key, limit, windowMs);
    }

    public Result check(String key, int customLimit, long customWindowMs) {
        String normalizedKey = (key == null || key.isBlank()) ? "unknown" : key;
        String bucketKey = hashKey(normalizedKey);
        if (inMemoryBuckets != null) {
            return checkInMemory(bucketKey, customLimit, customWindowMs);
        }
        if (customLimit <= 0 || customWindowMs <= 0) {
            return new Result(true, 0);
        }
        long now = nowMs.getAsLong();
        try {
            Object lock = keyLocks.computeIfAbsent(bucketKey, k -> new Object());
            synchronized (lock) {
                AtomicBoolean allowed = new AtomicBoolean(true);
                RateLimitBucket bucket = rateLimitBucketRepository.findByBucketKey(bucketKey).orElse(null);
                long effectiveWindow = bucket == null || bucket.getWindowMs() <= 0 ? customWindowMs : bucket.getWindowMs();
                if (bucket == null || now - bucket.getWindowStartMs() >= effectiveWindow) {
                    RateLimitBucket fresh = bucket == null ? new RateLimitBucket() : bucket;
                    fresh.setBucketKey(bucketKey);
                    fresh.setWindowStartMs(now);
                    fresh.setWindowMs(customWindowMs);
                    fresh.setExpiresAtMs(now + customWindowMs);
                    fresh.setCount(1);
                    fresh.setUpdatedAt(Instant.now());
                    rateLimitBucketRepository.save(fresh);
                    maybeCleanup(now);
                    return new Result(true, 0);
                }
                if (bucket.getCount() >= customLimit) {
                    allowed.set(false);
                } else {
                    bucket.setCount(bucket.getCount() + 1);
                    bucket.setWindowMs(customWindowMs);
                    bucket.setExpiresAtMs(bucket.getWindowStartMs() + customWindowMs);
                    bucket.setUpdatedAt(Instant.now());
                    rateLimitBucketRepository.save(bucket);
                }
                maybeCleanup(now);
                pruneLocks();
                if (allowed.get()) {
                    return new Result(true, 0);
                }
                long until = bucket.getWindowStartMs() + effectiveWindow;
                long retryAfter = Math.max(0, until - now);
                return new Result(false, retryAfter);
            }
        } catch (DataAccessException ex) {
            // Rate-limiter must not take the app down if storage is under lock pressure.
            log.warn("Rate limiter storage contention for key={}, allowing request. Cause={}",
                    bucketKey.substring(0, Math.min(bucketKey.length(), 18)), ex.getClass().getSimpleName());
            return new Result(true, 0);
        } catch (RuntimeException ex) {
            // Defensive fallback for lock-related runtime exceptions that might bypass Spring translation.
            log.warn("Rate limiter runtime contention for key={}, allowing request. Cause={}",
                    bucketKey.substring(0, Math.min(bucketKey.length(), 18)), ex.getClass().getSimpleName());
            return new Result(true, 0);
        }
    }

    @Transactional
    public void reset() {
        if (inMemoryBuckets != null) {
            inMemoryBuckets.clear();
        } else {
            rateLimitBucketRepository.deleteAll();
        }
    }

    private void evictExpired(long nowMs) {
        if (inMemoryBuckets != null) {
            inMemoryBuckets.entrySet().removeIf(entry -> {
                InMemoryBucket bucket = entry.getValue();
                long window = bucket.windowMs <= 0 ? windowMs : bucket.windowMs;
                return nowMs - bucket.windowStartMs >= window;
            });
        } else {
            List<String> expiredKeys = rateLimitBucketRepository.findExpiredBucketKeys(
                    nowMs,
                    PageRequest.of(0, Math.max(1, Math.min(200, maxEntries)))
            );
            if (expiredKeys == null || expiredKeys.isEmpty()) {
                return;
            }
            for (String key : expiredKeys) {
                if (key == null || key.isBlank()) {
                    continue;
                }
                try {
                    rateLimitBucketRepository.deleteById(key);
                } catch (DataAccessException ex) {
                    log.debug("Rate limiter expired cleanup skipped key={} due to DB contention: {}", shortKey(key), ex.getClass().getSimpleName());
                } catch (RuntimeException ex) {
                    log.debug("Rate limiter expired cleanup skipped key={} due to runtime contention: {}", shortKey(key), ex.getClass().getSimpleName());
                }
            }
            pruneLocks();
        }
    }

    private void evictIfNeeded() {
        if (inMemoryBuckets != null) {
            int size = inMemoryBuckets.size();
            if (size <= maxEntries) return;
            inMemoryBuckets.entrySet().stream()
                    .sorted((a, b) -> Long.compare(a.getValue().windowStartMs, b.getValue().windowStartMs))
                    .limit(Math.max(0, size - maxEntries))
                    .map(Map.Entry::getKey)
                    .forEach(inMemoryBuckets::remove);
        } else {
            long size = rateLimitBucketRepository.count();
            if (size <= maxEntries) {
                return;
            }
            int toRemove = (int) Math.max(0, size - maxEntries);
            List<RateLimitBucket> oldest = rateLimitBucketRepository.findTop100ByOrderByUpdatedAtAsc();
            for (RateLimitBucket b : oldest) {
                if (toRemove-- <= 0) break;
                if (b == null || b.getBucketKey() == null || b.getBucketKey().isBlank()) {
                    continue;
                }
                try {
                    rateLimitBucketRepository.deleteById(b.getBucketKey());
                } catch (DataAccessException ex) {
                    log.debug("Rate limiter capacity cleanup skipped key={} due to DB contention: {}",
                            shortKey(b.getBucketKey()), ex.getClass().getSimpleName());
                } catch (RuntimeException ex) {
                    log.debug("Rate limiter capacity cleanup skipped key={} due to runtime contention: {}",
                            shortKey(b.getBucketKey()), ex.getClass().getSimpleName());
                }
            }
        }
    }

    private void pruneLocks() {
        if (keyLocks == null) {
            return;
        }
        int limit = Math.max(maxEntries * 2, maxEntries + 1000);
        int size = keyLocks.size();
        if (size <= limit) {
            return;
        }
        int toRemove = size - maxEntries;
        Iterator<String> it = keyLocks.keySet().iterator();
        while (toRemove > 0 && it.hasNext()) {
            String key = it.next();
            keyLocks.remove(key);
            toRemove--;
        }
    }

    private void maybeCleanup(long nowMs) {
        if (inMemoryBuckets != null) {
            evictExpired(nowMs);
            evictIfNeeded();
            return;
        }
        if (cleanupInProgress == null || lastDbCleanupMs == null) {
            return;
        }
        long last = lastDbCleanupMs.get();
        if (cleanupIntervalMs > 0 && nowMs - last < cleanupIntervalMs) {
            return;
        }
        if (!cleanupInProgress.compareAndSet(false, true)) {
            return;
        }
        try {
            long latest = lastDbCleanupMs.get();
            if (cleanupIntervalMs > 0 && nowMs - latest < cleanupIntervalMs) {
                return;
            }
            runDbCleanupWithRetry(nowMs, 2);
            lastDbCleanupMs.set(nowMs);
        } catch (DataAccessException ex) {
            log.debug("Rate limiter cleanup skipped due to DB contention: {}", ex.getClass().getSimpleName());
        } catch (RuntimeException ex) {
            log.debug("Rate limiter cleanup skipped due to runtime contention: {}", ex.getClass().getSimpleName());
        } finally {
            cleanupInProgress.set(false);
        }
    }

    private void runDbCleanupWithRetry(long nowMs, int attempts) {
        int tries = Math.max(1, attempts);
        for (int attempt = 1; attempt <= tries; attempt += 1) {
            try {
                evictExpired(nowMs);
                evictIfNeeded();
                return;
            } catch (DataAccessException ex) {
                if (attempt >= tries) {
                    log.debug("Rate limiter cleanup exhausted retries due to DB contention: {}", ex.getClass().getSimpleName());
                    return;
                }
                backoffCleanupAttempt(attempt);
            } catch (RuntimeException ex) {
                if (attempt >= tries) {
                    log.debug("Rate limiter cleanup exhausted retries due to runtime contention: {}", ex.getClass().getSimpleName());
                    return;
                }
                backoffCleanupAttempt(attempt);
            }
        }
    }

    private void backoffCleanupAttempt(int attempt) {
        long delayMs = Math.max(2, Math.min(12, attempt * 4L));
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private String shortKey(String key) {
        if (key == null || key.isBlank()) {
            return "n/a";
        }
        return key.substring(0, Math.min(key.length(), 18));
    }

    public record Result(boolean allowed, long retryAfterMs) {
    }

    /**
     * Used in tests/diagnostics; not intended for request path logic.
     */
    int getApproximateBucketCount() {
        if (inMemoryBuckets != null) {
            return inMemoryBuckets.size();
        }
        return (int) rateLimitBucketRepository.count();
    }

    private Result checkInMemory(String key, int customLimit, long customWindowMs) {
        if (customLimit <= 0 || customWindowMs <= 0) {
            return new Result(true, 0);
        }
        long now = nowMs.getAsLong();
        AtomicBoolean allowed = new AtomicBoolean(true);
        inMemoryBuckets.compute(key, (k, bucket) -> {
            long effectiveWindow = bucket == null || bucket.windowMs <= 0 ? customWindowMs : bucket.windowMs;
            if (bucket == null || now - bucket.windowStartMs >= effectiveWindow) {
                InMemoryBucket fresh = new InMemoryBucket();
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
        if (allowed.get()) {
            return new Result(true, 0);
        }
        InMemoryBucket bucket = inMemoryBuckets.get(key);
        long effectiveWindow = bucket == null || bucket.windowMs <= 0 ? customWindowMs : bucket.windowMs;
        long until = (bucket == null ? now : bucket.windowStartMs) + effectiveWindow;
        long retryAfter = Math.max(0, until - now);
        return new Result(false, retryAfter);
    }

    private String hashKey(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return "rl:" + HexFormat.of().formatHex(digest);
        } catch (Exception ignored) {
            return "rl:" + Integer.toHexString(key.hashCode());
        }
    }

    private static final class InMemoryBucket {
        long windowStartMs;
        int count;
        long windowMs;
    }
}
