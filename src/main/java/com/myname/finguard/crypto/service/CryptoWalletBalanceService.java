package com.myname.finguard.crypto.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.crypto.model.CryptoNetwork;
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
public class CryptoWalletBalanceService {

    private static final long DEFAULT_CACHE_TTL_SECONDS = 60;

    private final CryptoWalletBalanceProvider provider;
    private final Duration cacheTtl;
    private final Clock clock;
    private final Map<String, CachedBalance> cache = new ConcurrentHashMap<>();

    public CryptoWalletBalanceService() {
        this(null, Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS), Clock.systemUTC());
    }

    @Autowired
    public CryptoWalletBalanceService(
            ObjectProvider<CryptoWalletBalanceProvider> provider,
            @Value("${app.crypto.wallet.cache-ttl-seconds:" + DEFAULT_CACHE_TTL_SECONDS + "}") long cacheTtlSeconds
    ) {
        this(provider.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, cacheTtlSeconds)),
                Clock.systemUTC());
    }

    CryptoWalletBalanceService(CryptoWalletBalanceProvider provider, Duration cacheTtl, Clock clock) {
        this.provider = provider;
        this.cacheTtl = cacheTtl == null ? Duration.ofSeconds(DEFAULT_CACHE_TTL_SECONDS) : cacheTtl;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public CryptoWalletBalanceProvider.WalletBalance latestBalance(CryptoNetwork network, String addressNormalized) {
        if (network == null || addressNormalized == null || addressNormalized.isBlank()) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Network and address are required", HttpStatus.BAD_REQUEST);
        }
        String key = network.name() + ":" + addressNormalized;
        Instant now = Instant.now(clock);
        CachedBalance cached = cache.get(key);
        if (cached != null && cacheTtl.compareTo(Duration.ZERO) > 0) {
            Duration age = Duration.between(cached.fetchedAt(), now);
            if (age.compareTo(cacheTtl) < 0) {
                return cached.balance();
            }
        }

        if (provider == null) {
            if (cached != null) {
                return cached.balance();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto wallet balance provider is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            CryptoWalletBalanceProvider.WalletBalance fetched = provider.fetchLatest(network, addressNormalized);
            if (fetched == null || fetched.balance() == null) {
                throw new IllegalStateException("Empty crypto wallet balance response");
            }
            cache.put(key, new CachedBalance(fetched, now));
            return fetched;
        } catch (Exception e) {
            if (cached != null) {
                return cached.balance();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to fetch crypto wallet balance", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record CachedBalance(CryptoWalletBalanceProvider.WalletBalance balance, Instant fetchedAt) {
    }
}

