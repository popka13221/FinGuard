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
public class EthWalletPortfolioService {

    private static final long DEFAULT_CACHE_TTL_SECONDS = 300;

    private final EthWalletPortfolioProvider provider;
    private final Duration cacheTtl;
    private final Clock clock;
    private final Map<String, CachedPortfolio> cache = new ConcurrentHashMap<>();

    public EthWalletPortfolioService() {
        this(null, Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS), Clock.systemUTC());
    }

    @Autowired
    public EthWalletPortfolioService(
            ObjectProvider<EthWalletPortfolioProvider> provider,
            @Value("${app.crypto.wallet.eth.portfolio.cache-ttl-seconds:" + DEFAULT_CACHE_TTL_SECONDS + "}") long cacheTtlSeconds
    ) {
        this(provider.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, cacheTtlSeconds)),
                Clock.systemUTC());
    }

    EthWalletPortfolioService(EthWalletPortfolioProvider provider, Duration cacheTtl, Clock clock) {
        this.provider = provider;
        this.cacheTtl = cacheTtl == null ? Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS) : cacheTtl;
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
            return fetched;
        } catch (Exception e) {
            if (cached != null) {
                return cached.portfolio();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to fetch ETH wallet portfolio", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record CachedPortfolio(EthWalletPortfolioProvider.EthWalletPortfolio portfolio, Instant fetchedAt) {
    }
}

