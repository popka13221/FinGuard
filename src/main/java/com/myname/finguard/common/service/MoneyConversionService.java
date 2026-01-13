package com.myname.finguard.common.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MoneyConversionService {

    private final CurrencyService currencyService;
    private final CryptoRatesService cryptoRatesService;

    public MoneyConversionService(CurrencyService currencyService, CryptoRatesService cryptoRatesService) {
        this.currencyService = currencyService;
        this.cryptoRatesService = cryptoRatesService;
    }

    public ConversionContext buildContext(String baseCurrency, Collection<String> currencies) {
        String base = normalizeCurrency(baseCurrency);
        if (base.isBlank() || !currencyService.isSupported(base)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported base currency", HttpStatus.BAD_REQUEST);
        }

        boolean needCrypto = isCrypto(base) || hasCrypto(currencies);
        boolean needFx = needsFxRates(base, currencies);

        Map<String, BigDecimal> fxUsd = needFx ? currencyService.latestRates("USD").rates() : Map.of();
        Map<String, BigDecimal> cryptoUsd = needCrypto ? fetchCryptoUsdPrices() : Map.of();

        int scale = isCrypto(base) ? 8 : 2;
        return new ConversionContext(base, scale, fxUsd, cryptoUsd);
    }

    public BigDecimal convertToBase(BigDecimal amount, String currency, ConversionContext ctx) {
        if (ctx == null) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Conversion context is required", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (amount == null) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }

        String from = normalizeCurrency(currency);
        if (from.isBlank() || !currencyService.isSupported(from)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported currency: " + from, HttpStatus.BAD_REQUEST);
        }
        if (from.equalsIgnoreCase(ctx.baseCurrency())) {
            return amount.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        if (amount.signum() == 0) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }

        BigDecimal usd = toUsd(amount, from, ctx);
        return fromUsd(usd, ctx);
    }

    public BigDecimal sumToBaseOrNull(String baseCurrency, Map<String, BigDecimal> totalsByCurrency) {
        if (totalsByCurrency == null || totalsByCurrency.isEmpty()) {
            String base = normalizeCurrency(baseCurrency);
            int scale = isCrypto(base) ? 8 : 2;
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }

        try {
            Map<String, BigDecimal> nonZero = new HashMap<>();
            for (var entry : totalsByCurrency.entrySet()) {
                String currency = normalizeCurrency(entry.getKey());
                BigDecimal amount = entry.getValue();
                if (currency.isBlank() || amount == null || amount.signum() == 0) {
                    continue;
                }
                nonZero.put(currency, amount);
            }

            if (nonZero.isEmpty()) {
                String base = normalizeCurrency(baseCurrency);
                int scale = isCrypto(base) ? 8 : 2;
                return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
            }

            ConversionContext ctx = buildContext(baseCurrency, nonZero.keySet());
            BigDecimal total = BigDecimal.ZERO;
            for (var entry : totalsByCurrency.entrySet()) {
                String currency = normalizeCurrency(entry.getKey());
                BigDecimal amount = entry.getValue();
                if (currency.isBlank() || amount == null) {
                    continue;
                }
                total = total.add(convertToBase(amount, currency, ctx));
            }
            return total.setScale(ctx.scale(), RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal toUsd(BigDecimal amount, String currency, ConversionContext ctx) {
        if ("USD".equalsIgnoreCase(currency)) {
            return amount;
        }
        if (isCrypto(currency)) {
            BigDecimal usdPrice = requireCryptoUsdPrice(ctx.cryptoUsdPrices(), currency);
            return amount.multiply(usdPrice);
        }
        BigDecimal rate = requireFxUsdRate(ctx.usdFxRates(), currency);
        return amount.divide(rate, 12, RoundingMode.HALF_UP);
    }

    private BigDecimal fromUsd(BigDecimal usdAmount, ConversionContext ctx) {
        String base = ctx.baseCurrency();
        if ("USD".equalsIgnoreCase(base)) {
            return usdAmount.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        if (isCrypto(base)) {
            BigDecimal baseUsdPrice = requireCryptoUsdPrice(ctx.cryptoUsdPrices(), base);
            return usdAmount.divide(baseUsdPrice, ctx.scale(), RoundingMode.HALF_UP);
        }
        BigDecimal baseRate = requireFxUsdRate(ctx.usdFxRates(), base);
        return usdAmount.multiply(baseRate).setScale(ctx.scale(), RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> fetchCryptoUsdPrices() {
        CryptoRatesProvider.CryptoRates rates = cryptoRatesService.latestRates("USD");
        if (rates == null || rates.rates() == null) {
            return Map.of();
        }
        Map<String, BigDecimal> prices = new HashMap<>();
        for (var item : rates.rates()) {
            if (item == null || item.code() == null || item.price() == null) {
                continue;
            }
            prices.putIfAbsent(item.code().trim().toUpperCase(Locale.ROOT), item.price());
        }
        return prices;
    }

    private BigDecimal requireFxUsdRate(Map<String, BigDecimal> usdFxRates, String currency) {
        if (usdFxRates == null || usdFxRates.isEmpty()) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rates are not available", HttpStatus.SERVICE_UNAVAILABLE);
        }
        BigDecimal rate = usdFxRates.get(currency);
        if (rate == null || rate.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rate is not available for currency: " + currency, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return rate;
    }

    private BigDecimal requireCryptoUsdPrice(Map<String, BigDecimal> cryptoUsdPrices, String currency) {
        if (cryptoUsdPrices == null || cryptoUsdPrices.isEmpty()) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto rates are not available", HttpStatus.SERVICE_UNAVAILABLE);
        }
        BigDecimal price = cryptoUsdPrices.get(currency);
        if (price == null || price.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto price is not available for currency: " + currency, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return price;
    }

    private boolean needsFxRates(String baseCurrency, Collection<String> currencies) {
        if (!isCrypto(baseCurrency) && !"USD".equalsIgnoreCase(baseCurrency)) {
            return true;
        }
        if (currencies == null || currencies.isEmpty()) {
            return false;
        }
        for (String c : currencies) {
            if (c == null || c.isBlank()) {
                continue;
            }
            String normalized = normalizeCurrency(c);
            if (!isCrypto(normalized) && !"USD".equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCrypto(Collection<String> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            return false;
        }
        for (String c : currencies) {
            if (isCrypto(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCrypto(String currency) {
        String normalized = normalizeCurrency(currency);
        return "BTC".equalsIgnoreCase(normalized) || "ETH".equalsIgnoreCase(normalized);
    }

    private String normalizeCurrency(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public record ConversionContext(
            String baseCurrency,
            int scale,
            Map<String, BigDecimal> usdFxRates,
            Map<String, BigDecimal> cryptoUsdPrices
    ) {
    }
}

