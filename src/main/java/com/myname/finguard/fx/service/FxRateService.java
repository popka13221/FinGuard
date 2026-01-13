package com.myname.finguard.fx.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.FxRatesProvider;
import com.myname.finguard.fx.model.FxRate;
import com.myname.finguard.fx.repository.FxRateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FxRateService {

    private final FxRateRepository fxRateRepository;
    private final CurrencyService currencyService;

    public FxRateService(FxRateRepository fxRateRepository, CurrencyService currencyService) {
        this.fxRateRepository = fxRateRepository;
        this.currencyService = currencyService;
    }

    public FxRatesProvider.FxRates latestStoredRates(String baseCurrency) {
        String base = normalize(baseCurrency);
        if (base.isBlank()) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Base currency is required", HttpStatus.BAD_REQUEST);
        }
        Instant asOf = fxRateRepository.findLatestAsOf(base);
        if (asOf == null) {
            return null;
        }
        List<FxRate> rows = fxRateRepository.findByBaseCurrencyAndAsOf(base, asOf);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Map<String, BigDecimal> rates = new HashMap<>();
        for (FxRate row : rows) {
            if (row == null || row.getQuoteCurrency() == null || row.getRate() == null) {
                continue;
            }
            rates.putIfAbsent(normalize(row.getQuoteCurrency()), row.getRate());
        }
        if (rates.isEmpty()) {
            return null;
        }
        return new FxRatesProvider.FxRates(base, asOf, rates);
    }

    @Transactional
    public FxRatesProvider.FxRates upsertSnapshot(String baseCurrency, Instant asOf, Map<String, BigDecimal> rates) {
        String base = normalize(baseCurrency);
        if (base.isBlank()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Base currency is required", HttpStatus.BAD_REQUEST);
        }
        if (!currencyService.isSupported(base) || isCrypto(base)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported base currency", HttpStatus.BAD_REQUEST);
        }
        if (asOf == null) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "`asOf` is required", HttpStatus.BAD_REQUEST);
        }
        if (rates == null || rates.isEmpty()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "`rates` must not be empty", HttpStatus.BAD_REQUEST);
        }

        Map<String, BigDecimal> normalized = new HashMap<>();
        for (var entry : rates.entrySet()) {
            String quote = normalize(entry.getKey());
            if (quote.isBlank() || quote.equalsIgnoreCase(base)) {
                continue;
            }
            if (!currencyService.isSupported(quote) || isCrypto(quote)) {
                throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported quote currency: " + quote, HttpStatus.BAD_REQUEST);
            }
            BigDecimal rate = entry.getValue();
            if (rate == null || rate.signum() <= 0) {
                throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Rate must be positive", HttpStatus.BAD_REQUEST);
            }
            normalized.put(quote, rate);
        }

        if (normalized.isEmpty()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "`rates` must contain at least one valid quote", HttpStatus.BAD_REQUEST);
        }

        fxRateRepository.deleteByBaseCurrencyAndAsOf(base, asOf);

        List<FxRate> entities = normalized.entrySet().stream()
                .map(entry -> {
                    FxRate fx = new FxRate();
                    fx.setBaseCurrency(base);
                    fx.setQuoteCurrency(entry.getKey());
                    fx.setRate(entry.getValue());
                    fx.setAsOf(asOf);
                    return fx;
                })
                .toList();
        fxRateRepository.saveAll(entities);

        currencyService.evictRatesCache(base);
        return new FxRatesProvider.FxRates(base, asOf, Map.copyOf(normalized));
    }

    public BigDecimal latestRateOrNull(String baseCurrency, String quoteCurrency) {
        String base = normalize(baseCurrency);
        String quote = normalize(quoteCurrency);
        if (base.isBlank() || quote.isBlank()) {
            return null;
        }
        return fxRateRepository.findTopByBaseCurrencyAndQuoteCurrencyOrderByAsOfDesc(base, quote)
                .map(FxRate::getRate)
                .orElse(null);
    }

    private boolean isCrypto(String currency) {
        String code = normalize(currency);
        return "BTC".equalsIgnoreCase(code) || "ETH".equalsIgnoreCase(code);
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
