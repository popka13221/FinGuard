package com.myname.finguard.common.service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ExternalProviderGuard {

    private static final Logger log = LoggerFactory.getLogger(ExternalProviderGuard.class);

    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;
    private final int circuitFailureThreshold;
    private final long circuitOpenMs;
    private final int budgetMaxEntries;

    private final Map<String, BudgetBucket> budgets = new ConcurrentHashMap<>();
    private final Map<String, CircuitState> circuits = new ConcurrentHashMap<>();

    public ExternalProviderGuard(
            @Value("${app.external.providers.retry.max-attempts:2}") int maxAttempts,
            @Value("${app.external.providers.retry.initial-backoff-ms:150}") long initialBackoffMs,
            @Value("${app.external.providers.retry.max-backoff-ms:1500}") long maxBackoffMs,
            @Value("${app.external.providers.circuit.failure-threshold:5}") int circuitFailureThreshold,
            @Value("${app.external.providers.circuit.open-ms:30000}") long circuitOpenMs,
            @Value("${app.external.providers.budget.max-entries:2000}") int budgetMaxEntries
    ) {
        this.maxAttempts = clamp(maxAttempts, 1, 5);
        this.initialBackoffMs = Math.max(0, initialBackoffMs);
        this.maxBackoffMs = Math.max(0, maxBackoffMs);
        this.circuitFailureThreshold = Math.max(0, circuitFailureThreshold);
        this.circuitOpenMs = Math.max(0, circuitOpenMs);
        this.budgetMaxEntries = clamp(budgetMaxEntries, 10, 200_000);
    }

    public <T> T execute(String providerKey, int budgetLimit, long budgetWindowMs, Supplier<T> call) {
        Objects.requireNonNull(call, "call");
        String key = normalizeKey(providerKey);

        if (!allowBudget(key, budgetLimit, budgetWindowMs)) {
            throw new ExternalProviderUnavailableException("External provider budget exceeded: " + key);
        }

        CircuitState circuit = circuits.computeIfAbsent(key, ignored -> new CircuitState());
        long now = System.currentTimeMillis();
        if (circuit.isOpen(now)) {
            throw new ExternalProviderUnavailableException("External provider circuit is open: " + key);
        }

        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T res = call.get();
                circuit.onSuccess();
                return res;
            } catch (Exception ex) {
                last = ex;
                boolean retryable = isRetryable(ex);
                boolean hasMore = attempt < maxAttempts;
                if (!retryable || !hasMore) {
                    circuit.onFailure(System.currentTimeMillis(), circuitFailureThreshold, circuitOpenMs);
                    throw ex;
                }
                sleep(backoffDelayMs(attempt));
                if (circuit.isOpen(System.currentTimeMillis())) {
                    throw new ExternalProviderUnavailableException("External provider circuit is open: " + key, ex);
                }
            }
        }
        throw new IllegalStateException("External provider call failed", last);
    }

    private boolean isRetryable(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return false;
        }
        if (ex instanceof RestClientResponseException http) {
            HttpStatus status = HttpStatus.resolve(http.getStatusCode().value());
            if (status == null) {
                return true;
            }
            if (status == HttpStatus.TOO_MANY_REQUESTS) {
                return true;
            }
            return status.is5xxServerError();
        }
        return ex instanceof RestClientException;
    }

    private long backoffDelayMs(int attempt) {
        if (attempt <= 1 || initialBackoffMs <= 0) {
            return 0;
        }
        long base = initialBackoffMs;
        int pow = Math.min(20, attempt - 2);
        long delay;
        try {
            delay = Math.multiplyExact(base, 1L << pow);
        } catch (ArithmeticException ignored) {
            delay = Long.MAX_VALUE;
        }
        if (maxBackoffMs > 0) {
            delay = Math.min(delay, maxBackoffMs);
        }
        if (delay <= 0) {
            return 0;
        }
        double jitter = 0.5 + ThreadLocalRandom.current().nextDouble();
        return Math.max(0, (long) Math.floor(delay * jitter));
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.debug("Interrupted during external provider backoff sleep");
        }
    }

    private boolean allowBudget(String key, int limit, long windowMs) {
        if (limit <= 0 || windowMs <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(true);
        budgets.compute(key, (k, bucket) -> {
            if (bucket == null || now - bucket.windowStartMs >= bucket.windowMs) {
                return new BudgetBucket(now, windowMs, 1, now);
            }
            if (bucket.count >= limit) {
                allowed.set(false);
                bucket.lastSeenMs = now;
                return bucket;
            }
            bucket.count += 1;
            bucket.lastSeenMs = now;
            bucket.windowMs = windowMs;
            return bucket;
        });
        evictBudgetExpired(now);
        evictBudgetIfNeeded();
        return allowed.get();
    }

    private void evictBudgetExpired(long nowMs) {
        budgets.entrySet().removeIf(entry -> {
            BudgetBucket bucket = entry.getValue();
            long window = bucket.windowMs <= 0 ? 0 : bucket.windowMs;
            if (window <= 0) {
                return false;
            }
            return nowMs - bucket.windowStartMs >= window && nowMs - bucket.lastSeenMs >= window;
        });
    }

    private void evictBudgetIfNeeded() {
        int size = budgets.size();
        if (size <= budgetMaxEntries) {
            return;
        }
        int toRemove = Math.max(0, size - budgetMaxEntries);
        budgets.entrySet().stream()
                .sorted((a, b) -> Long.compare(a.getValue().lastSeenMs, b.getValue().lastSeenMs))
                .limit(toRemove)
                .map(Map.Entry::getKey)
                .forEach(budgets::remove);
    }

    private String normalizeKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return "external";
        }
        return raw.trim().toLowerCase();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static class ExternalProviderUnavailableException extends RuntimeException {
        public ExternalProviderUnavailableException(String message) {
            super(message);
        }

        public ExternalProviderUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class BudgetBucket {
        final long windowStartMs;
        long windowMs;
        int count;
        long lastSeenMs;

        private BudgetBucket(long windowStartMs, long windowMs, int count, long lastSeenMs) {
            this.windowStartMs = windowStartMs;
            this.windowMs = windowMs;
            this.count = count;
            this.lastSeenMs = lastSeenMs;
        }
    }

    private static final class CircuitState {
        private int consecutiveFailures;
        private long openUntilMs;

        synchronized boolean isOpen(long nowMs) {
            if (openUntilMs <= 0) {
                return false;
            }
            if (nowMs >= openUntilMs) {
                openUntilMs = 0;
                consecutiveFailures = 0;
                return false;
            }
            return true;
        }

        synchronized void onSuccess() {
            consecutiveFailures = 0;
            openUntilMs = 0;
        }

        synchronized void onFailure(long nowMs, int failureThreshold, long openMs) {
            if (failureThreshold <= 0 || openMs <= 0) {
                return;
            }
            consecutiveFailures += 1;
            if (consecutiveFailures >= failureThreshold) {
                openUntilMs = nowMs + openMs;
                consecutiveFailures = 0;
            }
        }
    }
}

