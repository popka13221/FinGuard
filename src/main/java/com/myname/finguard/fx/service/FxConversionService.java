package com.myname.finguard.fx.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.FxRatesProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FxConversionService {

    private final CurrencyService currencyService;

    public FxConversionService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        return convert(amount, fromCurrency, toCurrency, 2);
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency, int scale) {
        if (amount == null) {
            return null;
        }
        String from = normalize(fromCurrency);
        String to = normalize(toCurrency);
        if (from.isBlank() || to.isBlank()) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Currency codes are required", HttpStatus.BAD_REQUEST);
        }
        if (!currencyService.isSupported(from) || isCrypto(from)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported from currency", HttpStatus.BAD_REQUEST);
        }
        if (!currencyService.isSupported(to) || isCrypto(to)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported to currency", HttpStatus.BAD_REQUEST);
        }
        if (from.equalsIgnoreCase(to)) {
            return amount.setScale(scale, RoundingMode.HALF_UP);
        }

        FxRatesProvider.FxRates usdRates = currencyService.latestRates("USD");
        Map<String, BigDecimal> rates = usdRates == null ? null : usdRates.rates();
        if (rates == null || rates.isEmpty()) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rates are not available", HttpStatus.SERVICE_UNAVAILABLE);
        }

        BigDecimal rateFrom = "USD".equalsIgnoreCase(from) ? BigDecimal.ONE : rates.get(from);
        BigDecimal rateTo = "USD".equalsIgnoreCase(to) ? BigDecimal.ONE : rates.get(to);
        if (rateFrom == null || rateFrom.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rate is not available for currency: " + from, HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (rateTo == null || rateTo.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rate is not available for currency: " + to, HttpStatus.SERVICE_UNAVAILABLE);
        }

        BigDecimal usd = "USD".equalsIgnoreCase(from) ? amount : amount.divide(rateFrom, 12, RoundingMode.HALF_UP);
        BigDecimal converted = "USD".equalsIgnoreCase(to) ? usd : usd.multiply(rateTo);
        return converted.setScale(scale, RoundingMode.HALF_UP);
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

