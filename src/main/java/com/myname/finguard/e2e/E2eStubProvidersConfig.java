package com.myname.finguard.e2e;

import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.FxRatesProvider;
import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.service.CryptoWalletBalanceProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("e2e")
public class E2eStubProvidersConfig {

    private static final Instant FIXED_AS_OF = Instant.parse("2024-01-01T00:00:00Z");

    // Anchor: 1 USD = X QUOTE
    private static final Map<String, BigDecimal> USD_FX = Map.of(
            "USD", BigDecimal.ONE,
            "EUR", new BigDecimal("0.9"),
            "RUB", new BigDecimal("90"),
            "CNY", new BigDecimal("7")
    );

    // Anchor: 1 BTC/ETH/SOL = X USD
    private static final Map<String, BigDecimal> CRYPTO_USD = Map.of(
            "BTC", new BigDecimal("65000"),
            "ETH", new BigDecimal("3000"),
            "SOL", new BigDecimal("120")
    );

    @Bean
    @Primary
    public FxRatesProvider fxRatesProvider() {
        return baseCurrency -> {
            String base = normalize(baseCurrency);
            BigDecimal basePerUsd = USD_FX.getOrDefault(base, BigDecimal.ONE);

            Map<String, BigDecimal> rates = USD_FX.entrySet().stream()
                    .filter(entry -> !entry.getKey().equalsIgnoreCase(base))
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().divide(basePerUsd, 12, RoundingMode.HALF_UP)
                    ));

            return new FxRatesProvider.FxRates(base, FIXED_AS_OF, rates);
        };
    }

    @Bean
    @Primary
    public CryptoRatesProvider cryptoRatesProvider() {
        return baseCurrency -> {
            String base = normalize(baseCurrency);
            List<CryptoRatesProvider.CryptoRate> rates = CRYPTO_USD.entrySet().stream()
                    .map(entry -> new CryptoRatesProvider.CryptoRate(
                            entry.getKey(),
                            entry.getKey(),
                            toBase(entry.getValue(), base),
                            BigDecimal.ZERO,
                            List.of()
                    ))
                    .toList();
            return new CryptoRatesProvider.CryptoRates(base, FIXED_AS_OF, rates);
        };
    }

    @Bean
    @Primary
    public CryptoWalletBalanceProvider cryptoWalletBalanceProvider() {
        return (network, addressNormalized) -> {
            if (network == null) {
                throw new IllegalArgumentException("Network is required");
            }
            if (addressNormalized == null || addressNormalized.isBlank()) {
                throw new IllegalArgumentException("Address is required");
            }
            BigDecimal balance = switch (network) {
                case BTC -> new BigDecimal("0.12345678");
                case ETH -> new BigDecimal("1.50000000");
            };
            return new CryptoWalletBalanceProvider.WalletBalance(network, addressNormalized, balance, FIXED_AS_OF);
        };
    }

    private static BigDecimal toBase(BigDecimal usdPrice, String baseCurrency) {
        if (usdPrice == null) {
            return null;
        }
        String base = normalize(baseCurrency);
        if ("USD".equalsIgnoreCase(base)) {
            return usdPrice;
        }
        if ("BTC".equalsIgnoreCase(base)) {
            return usdPrice.divide(CRYPTO_USD.get("BTC"), 12, RoundingMode.HALF_UP);
        }
        if ("ETH".equalsIgnoreCase(base)) {
            return usdPrice.divide(CRYPTO_USD.get("ETH"), 12, RoundingMode.HALF_UP);
        }
        BigDecimal basePerUsd = USD_FX.get(base);
        if (basePerUsd != null) {
            return usdPrice.multiply(basePerUsd);
        }
        return usdPrice;
    }

    private static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return "USD";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
