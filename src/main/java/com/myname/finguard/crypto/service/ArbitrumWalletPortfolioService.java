package com.myname.finguard.crypto.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ArbitrumWalletPortfolioService {

    private static final long DEFAULT_CACHE_TTL_SECONDS = 300;

    private final ArbitrumWalletPortfolioProvider provider;
    private final Duration cacheTtl;
    private final Clock clock;
    private final Map<String, CachedPortfolio> cache = new ConcurrentHashMap<>();

    public ArbitrumWalletPortfolioService() {
        this(null, Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS), Clock.systemUTC());
    }

    @Autowired
    public ArbitrumWalletPortfolioService(
            ObjectProvider<ArbitrumWalletPortfolioProvider> provider,
            @Value("${app.crypto.wallet.arbitrum.portfolio.cache-ttl-seconds:" + DEFAULT_CACHE_TTL_SECONDS + "}") long cacheTtlSeconds
    ) {
        this(provider.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, cacheTtlSeconds)),
                Clock.systemUTC());
    }

    ArbitrumWalletPortfolioService(ArbitrumWalletPortfolioProvider provider, Duration cacheTtl, Clock clock) {
        this.provider = provider;
        this.cacheTtl = cacheTtl == null ? Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS) : cacheTtl;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public ArbitrumWalletPortfolioProvider.ArbitrumWalletPortfolio latestPortfolio(String addressNormalized) {
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
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Arbitrum wallet portfolio provider is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            ArbitrumWalletPortfolioProvider.ArbitrumWalletPortfolio fetched = provider.fetchLatest(key);
            if (fetched == null) {
                throw new IllegalStateException("Empty Arbitrum wallet portfolio response");
            }
            cache.put(key, new CachedPortfolio(fetched, now));
            return fetched;
        } catch (Exception e) {
            if (cached != null) {
                return cached.portfolio();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to fetch Arbitrum wallet portfolio", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record CachedPortfolio(ArbitrumWalletPortfolioProvider.ArbitrumWalletPortfolio portfolio, Instant fetchedAt) {
    }
}

