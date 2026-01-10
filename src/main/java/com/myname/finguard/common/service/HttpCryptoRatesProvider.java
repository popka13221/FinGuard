package com.myname.finguard.common.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class HttpCryptoRatesProvider implements CryptoRatesProvider {

    private static final String PROVIDER_KEY = "coingecko";

    private static final List<CryptoAsset> ASSETS = List.of(
            new CryptoAsset("bitcoin", "BTC", "Bitcoin"),
            new CryptoAsset("ethereum", "ETH", "Ethereum"),
            new CryptoAsset("solana", "SOL", "Solana")
    );

    private final RestClient restClient;
    private final ExternalProviderGuard guard;
    private final int budgetLimit;
    private final long budgetWindowMs;

    public HttpCryptoRatesProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.provider-base-url:https://api.coingecko.com/api/v3}") String baseUrl
    ) {
        this(builder, baseUrl, null, 0, 0);
    }

    @Autowired
    public HttpCryptoRatesProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.provider-base-url:https://api.coingecko.com/api/v3}") String baseUrl,
            ExternalProviderGuard guard,
            @Value("${app.external.providers.budget.coingecko.limit:120}") int budgetLimit,
            @Value("${app.external.providers.budget.coingecko.window-ms:60000}") long budgetWindowMs
    ) {
        this.restClient = builder.baseUrl(trimTrailingSlash(baseUrl)).build();
        this.guard = guard;
        this.budgetLimit = Math.max(0, budgetLimit);
        this.budgetWindowMs = Math.max(0, budgetWindowMs);
    }

    @Override
    public CryptoRates fetchLatest(String baseCurrency) {
        String base = normalizeBase(baseCurrency);
        String ids = ASSETS.stream().map(CryptoAsset::id).collect(Collectors.joining(","));
        CoinGeckoMarket[] response = guarded(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/coins/markets")
                        .queryParam("vs_currency", base)
                        .queryParam("ids", ids)
                        .queryParam("sparkline", "true")
                        .queryParam("price_change_percentage", "24h")
                        .build())
                .retrieve()
                .body(CoinGeckoMarket[].class));
        if (response == null || response.length == 0) {
            throw new IllegalStateException("Empty crypto rates response");
        }
        Map<String, CoinGeckoMarket> byId = Arrays.stream(response)
                .filter(item -> item != null && item.id() != null)
                .collect(Collectors.toMap(item -> item.id().toLowerCase(), Function.identity(), (a, b) -> a));
        Instant asOf = Instant.now();
        List<CryptoRate> rates = ASSETS.stream()
                .map(asset -> toCryptoRate(asset, byId.get(asset.id())))
                .filter(rate -> rate != null && rate.price() != null)
                .toList();
        if (rates.isEmpty()) {
            throw new IllegalStateException("No crypto rates matched assets");
        }
        return new CryptoRates(base.toUpperCase(), asOf, rates);
    }

    private <T> T guarded(Supplier<T> call) {
        if (guard == null) {
            return call.get();
        }
        return guard.execute(PROVIDER_KEY, budgetLimit, budgetWindowMs, call);
    }

    private CryptoRate toCryptoRate(CryptoAsset asset, CoinGeckoMarket market) {
        if (asset == null || market == null || market.current_price() == null) {
            return null;
        }
        List<BigDecimal> sparkline = market.sparkline_in_7d() != null && market.sparkline_in_7d().price() != null
                ? market.sparkline_in_7d().price()
                : List.of();
        return new CryptoRate(
                asset.code(),
                asset.name(),
                market.current_price(),
                market.price_change_percentage_24h(),
                sparkline
        );
    }

    private String normalizeBase(String baseCurrency) {
        if (baseCurrency == null || baseCurrency.isBlank()) {
            return "usd";
        }
        return baseCurrency.trim().toLowerCase();
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    private record CryptoAsset(String id, String code, String name) {
    }

    private record CoinGeckoMarket(
            String id,
            String symbol,
            String name,
            BigDecimal current_price,
            BigDecimal price_change_percentage_24h,
            Sparkline sparkline_in_7d
    ) {
    }

    private record Sparkline(List<BigDecimal> price) {
    }
}
