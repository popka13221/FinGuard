package com.myname.finguard.crypto.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EthWalletPortfolioService {

    private static final long DEFAULT_CACHE_TTL_SECONDS = 300;
    private static final int DEFAULT_CACHE_MAX_ENTRIES = 5000;

    private final EthWalletPortfolioProvider provider;
    private final Duration cacheTtl;
    private final int maxEntries;
    private final Clock clock;
    private final Map<String, CachedPortfolio> cache = new ConcurrentHashMap<>();

    public EthWalletPortfolioService() {
        this(null, Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS), DEFAULT_CACHE_MAX_ENTRIES, Clock.systemUTC());
    }

    @Autowired
    public EthWalletPortfolioService(
            ObjectProvider<EthWalletPortfolioProvider> provider,
            @Value("${app.crypto.wallet.eth.portfolio.cache-ttl-seconds:" + DEFAULT_CACHE_TTL_SECONDS + "}") long cacheTtlSeconds,
            @Value("${app.crypto.wallet.eth.portfolio.cache-max-entries:" + DEFAULT_CACHE_MAX_ENTRIES + "}") int maxEntries
    ) {
        this(provider.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, cacheTtlSeconds)),
                Math.max(0, maxEntries),
                Clock.systemUTC());
    }

    EthWalletPortfolioService(EthWalletPortfolioProvider provider, Duration cacheTtl, int maxEntries, Clock clock) {
        this.provider = provider;
        this.cacheTtl = cacheTtl == null ? Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS) : cacheTtl;
        this.maxEntries = Math.max(0, maxEntries);
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public EthWalletPortfolioProvider.EthWalletPortfolio latestPortfolio(String addressNormalized) {
        if (addressNormalized == null || addressNormalized.isBlank()) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Address is required", HttpStatus.BAD_REQUEST);
        }
        String key = addressNormalized.trim().toLowerCase();
        Instant now = Instant.now(clock);
        CachedPortfolio cached = cache.get(key);
        if (cached != null && cacheTtl.compareTo(Duration.ZERO) > 0) {
            Duration age = Duration.between(cached.fetchedAt(), now);
            if (age.compareTo(cacheTtl) < 0) {
                return cached.portfolio();
            }
        }

        if (provider == null) {
            if (cached != null) {
                return cached.portfolio();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "ETH wallet portfolio provider is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            EthWalletPortfolioProvider.EthWalletPortfolio fetched = provider.fetchLatest(key);
            if (fetched == null) {
                throw new IllegalStateException("Empty ETH wallet portfolio response");
            }
            cache.put(key, new CachedPortfolio(fetched, now));
            evictIfNeeded();
            return fetched;
        } catch (Exception e) {
            if (cached != null) {
                return cached.portfolio();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to fetch ETH wallet portfolio", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void evictIfNeeded() {
        if (maxEntries <= 0) {
            return;
        }
        int size = cache.size();
        if (size <= maxEntries) {
            return;
        }
        int toRemove = size - maxEntries;
        cache.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().fetchedAt()))
                .limit(toRemove)
                .map(Map.Entry::getKey)
                .forEach(cache::remove);
    }

    private record CachedPortfolio(EthWalletPortfolioProvider.EthWalletPortfolio portfolio, Instant fetchedAt) {
    }
}
