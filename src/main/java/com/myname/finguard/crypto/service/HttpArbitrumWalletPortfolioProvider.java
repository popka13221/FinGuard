package com.myname.finguard.crypto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.external.providers.enabled", havingValue = "true", matchIfMissing = true)
public class HttpArbitrumWalletPortfolioProvider implements ArbitrumWalletPortfolioProvider {

    private static final int TOP_TOKENS_LIMIT = 5;
    private static final int INTERNAL_USD_SCALE = 12;
    private static final int PRICE_BATCH_SIZE = 50;
    private static final int MAX_TOKENS_SCANNED = 200;
    private static final int MAX_TOKEN_DECIMALS = 30;
    private static final int MAX_RAW_BALANCE_LENGTH = 120;

    private static final Pattern CONTRACT_ADDRESS = Pattern.compile("^0x[0-9a-f]{40}$");

    private final RestClient blockscoutClient;
    private final RestClient pricesClient;

    public HttpArbitrumWalletPortfolioProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.wallet.arbitrum.portfolio.blockscout-base-url:https://arbitrum.blockscout.com}") String blockscoutBaseUrl,
            @Value("${app.crypto.wallet.arbitrum.portfolio.prices-base-url:https://coins.llama.fi}") String pricesBaseUrl
    ) {
        this.blockscoutClient = builder.baseUrl(trimTrailingSlash(blockscoutBaseUrl)).build();
        this.pricesClient = builder.baseUrl(trimTrailingSlash(pricesBaseUrl)).build();
    }

    @Override
    public ArbitrumWalletPortfolio fetchLatest(String addressNormalized) {
        if (addressNormalized == null || addressNormalized.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        BlockscoutTokenListResponse response = blockscoutClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api")
                        .queryParam("module", "account")
                        .queryParam("action", "tokenlist")
                        .queryParam("address", addressNormalized)
                        .build())
                .retrieve()
                .body(BlockscoutTokenListResponse.class);

        if (response == null || response.result() == null) {
            throw new IllegalStateException("Empty Arbitrum token list response");
        }

        List<TokenBalance> tokens = response.result().stream()
                .filter(Objects::nonNull)
                .filter(entry -> entry.contractAddress() != null && !entry.contractAddress().isBlank())
                .filter(entry -> entry.balance() != null && !entry.balance().isBlank())
                .filter(entry -> entry.type() == null || "ERC-20".equalsIgnoreCase(entry.type()))
                .map(TokenBalance::fromBlockscout)
                .filter(t -> t.contractAddress() != null && !t.contractAddress().isBlank())
                .filter(t -> t.amount() != null && t.amount().signum() > 0)
                .limit(MAX_TOKENS_SCANNED)
                .toList();

        if (tokens.isEmpty()) {
            return new ArbitrumWalletPortfolio(addressNormalized, Instant.now(), BigDecimal.ZERO, List.of());
        }

        Map<String, BigDecimal> prices = fetchUsdPrices(tokens);
        List<TokenHolding> holdings = new ArrayList<>();
        BigDecimal totalUsd = BigDecimal.ZERO;
        for (TokenBalance token : tokens) {
            BigDecimal priceUsd = prices.get(token.contractAddress().toLowerCase(Locale.ROOT));
            if (priceUsd == null || priceUsd.signum() <= 0) {
                continue;
            }
            BigDecimal valueUsd = token.amount().multiply(priceUsd).setScale(INTERNAL_USD_SCALE, RoundingMode.HALF_UP);
            totalUsd = totalUsd.add(valueUsd);
            holdings.add(new TokenHolding(token.contractAddress(), token.symbol(), token.amount(), priceUsd, valueUsd));
        }

        holdings.sort(Comparator.comparing(TokenHolding::valueUsd).reversed());
        List<TokenHolding> top = holdings.size() <= TOP_TOKENS_LIMIT ? holdings : holdings.subList(0, TOP_TOKENS_LIMIT);
        return new ArbitrumWalletPortfolio(addressNormalized, Instant.now(), totalUsd, List.copyOf(top));
    }

    private Map<String, BigDecimal> fetchUsdPrices(List<TokenBalance> tokens) {
        List<String> contracts = tokens.stream()
                .map(TokenBalance::contractAddress)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (contracts.isEmpty()) {
            return Map.of();
        }

        Map<String, BigDecimal> prices = new java.util.HashMap<>();
        for (int i = 0; i < contracts.size(); i += PRICE_BATCH_SIZE) {
            List<String> batch = contracts.subList(i, Math.min(i + PRICE_BATCH_SIZE, contracts.size()));
            String coins = batch.stream()
                    .map(contract -> "arbitrum:" + contract.toLowerCase(Locale.ROOT))
                    .collect(Collectors.joining(","));

            LlamaPricesResponse response = pricesClient.get()
                    .uri("/prices/current/{coins}", coins)
                    .retrieve()
                    .body(LlamaPricesResponse.class);
            if (response == null || response.coins() == null || response.coins().isEmpty()) {
                continue;
            }
            response.coins().forEach((key, coin) -> {
                if (coin == null || coin.price() == null || coin.price().signum() <= 0) {
                    return;
                }
                String contract = extractContract(key);
                contract = normalizeContract(contract);
                if (contract.isBlank()) {
                    return;
                }
                prices.put(contract.toLowerCase(Locale.ROOT), coin.price());
            });
        }
        return prices;
    }

    private String extractContract(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String raw = key.trim();
        int colon = raw.indexOf(':');
        if (colon < 0) {
            return "";
        }
        return raw.substring(colon + 1).trim();
    }

    private static String normalizeContract(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (!value.startsWith("0x")) {
            value = "0x" + value;
        }
        return CONTRACT_ADDRESS.matcher(value).matches() ? value : "";
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    private record BlockscoutTokenListResponse(String message, List<BlockscoutTokenEntry> result) {
    }

    private record BlockscoutTokenEntry(
            String balance,
            String contractAddress,
            String decimals,
            String symbol,
            String type
    ) {
    }

    private record LlamaPricesResponse(Map<String, LlamaCoin> coins) {
    }

    private record LlamaCoin(BigDecimal price) {
    }

    private record TokenBalance(String contractAddress, String symbol, BigDecimal amount) {

        static TokenBalance fromBlockscout(BlockscoutTokenEntry entry) {
            String contract = normalizeContract(entry.contractAddress());
            String symbol = entry.symbol() == null ? "" : entry.symbol().trim().toUpperCase(Locale.ROOT);
            int decimals = parseInt(entry.decimals(), 0);
            if (decimals < 0 || decimals > MAX_TOKEN_DECIMALS) {
                return new TokenBalance(contract, symbol, null);
            }
            BigDecimal raw = parseBigDecimal(entry.balance());
            if (raw == null || raw.signum() <= 0) {
                return new TokenBalance(contract, symbol, null);
            }
            BigDecimal amount = decimals <= 0 ? raw : raw.movePointLeft(decimals);
            return new TokenBalance(contract, symbol, amount);
        }
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static BigDecimal parseBigDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if (raw.length() > MAX_RAW_BALANCE_LENGTH) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
