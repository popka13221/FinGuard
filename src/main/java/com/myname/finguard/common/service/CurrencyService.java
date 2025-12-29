package com.myname.finguard.common.service;

import com.myname.finguard.common.dto.CurrencyDto;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private static final List<CurrencyDto> SUPPORTED = List.of(
            new CurrencyDto("USD", "US Dollar"),
            new CurrencyDto("EUR", "Euro"),
            new CurrencyDto("RUB", "Russian Ruble"),
            new CurrencyDto("CNY", "Chinese Yuan"),
            new CurrencyDto("BTC", "Bitcoin"),
            new CurrencyDto("ETH", "Ethereum")
    );

    private static final long DEFAULT_RATES_CACHE_TTL_SECONDS = 300;

    private final FxRatesProvider fxRatesProvider;
    private final Duration ratesCacheTtl;
    private final Clock clock;
    private final Map<String, CachedRates> ratesCache = new ConcurrentHashMap<>();

    public CurrencyService() {
        this(null, Duration.ofSeconds(DEFAULT_RATES_CACHE_TTL_SECONDS), Clock.systemUTC());
    }

    @Autowired
    public CurrencyService(
            ObjectProvider<FxRatesProvider> fxRatesProvider,
            @Value("${app.fx.cache-ttl-seconds:" + DEFAULT_RATES_CACHE_TTL_SECONDS + "}") long ratesCacheTtlSeconds
    ) {
        this(fxRatesProvider.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, ratesCacheTtlSeconds)),
                Clock.systemUTC());
    }

    CurrencyService(FxRatesProvider fxRatesProvider, Duration ratesCacheTtl, Clock clock) {
        this.fxRatesProvider = fxRatesProvider;
        this.ratesCacheTtl = ratesCacheTtl == null ? Duration.ofSeconds(DEFAULT_RATES_CACHE_TTL_SECONDS) : ratesCacheTtl;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public List<CurrencyDto> supportedCurrencies() {
        return SUPPORTED;
    }

    public String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    public boolean isSupported(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String normalized = normalize(code);
        return SUPPORTED.stream().anyMatch(c -> c.code().equalsIgnoreCase(normalized));
    }

    public FxRatesProvider.FxRates latestRates(String baseCurrency) {
        String base = normalize(baseCurrency);
        if (!isSupported(base)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported base currency", HttpStatus.BAD_REQUEST);
        }

        Instant now = Instant.now(clock);
        CachedRates cached = ratesCache.get(base);
        if (cached != null && ratesCacheTtl.compareTo(Duration.ZERO) > 0) {
            Duration age = Duration.between(cached.fetchedAt(), now);
            if (age.compareTo(ratesCacheTtl) < 0) {
                return cached.rates();
            }
        }

        if (fxRatesProvider == null) {
            if (cached != null) {
                return cached.rates();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rates provider is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            FxRatesProvider.FxRates fetched = fxRatesProvider.fetchLatest(base);
            if (fetched == null || fetched.rates() == null || fetched.rates().isEmpty()) {
                throw new IllegalStateException("Empty FX rates response");
            }
            ratesCache.put(base, new CachedRates(fetched, now));
            return fetched;
        } catch (Exception e) {
            if (cached != null) {
                return cached.rates();
            }
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to fetch FX rates", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record CachedRates(FxRatesProvider.FxRates rates, Instant fetchedAt) {
    }
}
