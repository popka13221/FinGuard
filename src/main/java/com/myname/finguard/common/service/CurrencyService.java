package com.myname.finguard.common.service;

import com.myname.finguard.common.dto.CurrencyDto;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.fx.model.FxRate;
import com.myname.finguard.fx.repository.FxRateRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.HashMap;
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
    private final FxRateRepository fxRateRepository;
    private final Duration ratesCacheTtl;
    private final Clock clock;
    private final Map<String, CachedRates> ratesCache = new ConcurrentHashMap<>();

    public CurrencyService() {
        this(null, null, Duration.ofSeconds(DEFAULT_RATES_CACHE_TTL_SECONDS), Clock.systemUTC());
    }

    @Autowired
    public CurrencyService(
            ObjectProvider<FxRatesProvider> fxRatesProvider,
            ObjectProvider<FxRateRepository> fxRateRepository,
            @Value("${app.fx.cache-ttl-seconds:" + DEFAULT_RATES_CACHE_TTL_SECONDS + "}") long ratesCacheTtlSeconds
    ) {
        this(fxRatesProvider.getIfAvailable(),
                fxRateRepository.getIfAvailable(),
                Duration.ofSeconds(Math.max(0, ratesCacheTtlSeconds)),
                Clock.systemUTC());
    }

    CurrencyService(FxRatesProvider fxRatesProvider, Duration ratesCacheTtl, Clock clock) {
        this(fxRatesProvider, null, ratesCacheTtl, clock);
    }

    CurrencyService(FxRatesProvider fxRatesProvider, FxRateRepository fxRateRepository, Duration ratesCacheTtl, Clock clock) {
        this.fxRatesProvider = fxRatesProvider;
        this.fxRateRepository = fxRateRepository;
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

        FxRatesProvider.FxRates stored = latestStoredRates(base);
        if (stored != null) {
            ratesCache.put(base, new CachedRates(stored, now));
            return stored;
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

    public void evictRatesCache(String baseCurrency) {
        String base = normalize(baseCurrency);
        if (!base.isBlank()) {
            ratesCache.remove(base);
        }
    }

    private FxRatesProvider.FxRates latestStoredRates(String baseCurrency) {
        if (fxRateRepository == null || baseCurrency == null || baseCurrency.isBlank()) {
            return null;
        }
        String base = normalize(baseCurrency);
        Instant asOf = fxRateRepository.findLatestAsOf(base);
        if (asOf == null) {
            return null;
        }
        var rows = fxRateRepository.findByBaseCurrencyAndAsOf(base, asOf);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Map<String, java.math.BigDecimal> rates = new HashMap<>();
        for (FxRate row : rows) {
            if (row == null || row.getQuoteCurrency() == null || row.getRate() == null) {
                continue;
            }
            String quote = normalize(row.getQuoteCurrency());
            if (quote.isBlank()) {
                continue;
            }
            rates.putIfAbsent(quote, row.getRate());
        }
        if (rates.isEmpty()) {
            return null;
        }
        return new FxRatesProvider.FxRates(base, asOf, Map.copyOf(rates));
    }

    private record CachedRates(FxRatesProvider.FxRates rates, Instant fetchedAt) {
    }
}
