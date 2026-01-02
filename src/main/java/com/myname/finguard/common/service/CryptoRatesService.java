package com.myname.finguard.common.service;

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
public class CryptoRatesService {

    private static final long DEFAULT_RATES_CACHE_TTL_SECONDS = 120;

    private final CryptoRatesProvider cryptoRatesProvider;
    private final Duration ratesCacheTtl;
    private final Clock clock;
    private final Map<String, CachedRates> ratesCache = new ConcurrentHashMap<>();

    public CryptoRatesService() {
        this(null, Duration.ofSeconds(DEFAULT_RATES_CACHE_TTL_SECONDS), Clock.systemUTC());
    }

    @Autowired
    public CryptoRatesService(
            ObjectProvider<CryptoRatesProvider> cryptoRatesProvider,
            @Value("${app.crypto.cache-ttl-seconds:" + DEFAULT_RATES_CACHE_TTL_SECONDS + "}") long ratesCacheTtlSeconds
    ) {
        this(cryptoRatesProvider.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, ratesCacheTtlSeconds)),
                Clock.systemUTC());
    }

    CryptoRatesService(CryptoRatesProvider cryptoRatesProvider, Duration ratesCacheTtl, Clock clock) {
        this.cryptoRatesProvider = cryptoRatesProvider;
        this.ratesCacheTtl = ratesCacheTtl == null ? Duration.ofSeconds(DEFAULT_RATES_CACHE_TTL_SECONDS) : ratesCacheTtl;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public CryptoRatesProvider.CryptoRates latestRates(String baseCurrency) {
        String base = normalizeBase(baseCurrency);
        Instant now = Instant.now(clock);
        CachedRates cached = ratesCache.get(base);
        if (cached != null && ratesCacheTtl.compareTo(Duration.ZERO) > 0) {
            Duration age = Duration.between(cached.fetchedAt(), now);
            if (age.compareTo(ratesCacheTtl) < 0) {
                return cached.rates();
            }
        }

        if (cryptoRatesProvider == null) {
            if (cached != null) {
                return cached.rates();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto rates provider is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            CryptoRatesProvider.CryptoRates fetched = cryptoRatesProvider.fetchLatest(base);
            if (fetched == null || fetched.rates() == null || fetched.rates().isEmpty()) {
                throw new IllegalStateException("Empty crypto rates response");
            }
            ratesCache.put(base, new CachedRates(fetched, now));
            return fetched;
        } catch (Exception e) {
            if (cached != null) {
                return cached.rates();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to fetch crypto rates", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String normalizeBase(String baseCurrency) {
        if (baseCurrency == null || baseCurrency.isBlank()) {
            return "USD";
        }
        return baseCurrency.trim().toUpperCase();
    }

    private record CachedRates(CryptoRatesProvider.CryptoRates rates, Instant fetchedAt) {
    }
}
