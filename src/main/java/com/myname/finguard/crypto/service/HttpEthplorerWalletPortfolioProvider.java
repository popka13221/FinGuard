package com.myname.finguard.crypto.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpEthplorerWalletPortfolioProvider implements EthWalletPortfolioProvider {

    private static final int TOP_TOKENS_LIMIT = 5;
    private static final int INTERNAL_USD_SCALE = 12;

    private static final Set<String> STABLECOINS = Set.of(
            "USDT",
            "USDC",
            "DAI",
            "BUSD",
            "TUSD",
            "USDP",
            "GUSD",
            "FDUSD"
    );

    private final RestClient restClient;
    private final String apiKey;

    public HttpEthplorerWalletPortfolioProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.wallet.eth.portfolio.provider-base-url:https://api.ethplorer.io}") String baseUrl,
            @Value("${app.crypto.wallet.eth.portfolio.api-key:freekey}") String apiKey
    ) {
        this.restClient = builder.baseUrl(trimTrailingSlash(baseUrl)).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    @Override
    public EthWalletPortfolio fetchLatest(String addressNormalized) {
        if (addressNormalized == null || addressNormalized.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Ethplorer apiKey is not configured");
        }

        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAddressInfo/{address}")
                        .queryParam("apiKey", apiKey)
                        .build(addressNormalized))
                .retrieve()
                .body(JsonNode.class);
        if (response == null || response.isNull()) {
            throw new IllegalStateException("Empty ETH portfolio response");
        }

        Instant asOf = Instant.now();
        ParsedTokens parsed = parseTokens(response);
        return new EthWalletPortfolio(addressNormalized, asOf, parsed.totalUsd(), parsed.topTokens());
    }

    static ParsedTokens parseTokens(JsonNode response) {
        if (response == null || response.isNull()) {
            return new ParsedTokens(BigDecimal.ZERO, List.of());
        }
        JsonNode tokens = response.get("tokens");
        if (tokens == null || !tokens.isArray()) {
            return new ParsedTokens(BigDecimal.ZERO, List.of());
        }

        List<TokenHolding> holdings = new ArrayList<>();
        BigDecimal totalUsd = BigDecimal.ZERO;
        Set<String> seen = new HashSet<>();

        for (JsonNode token : tokens) {
            if (token == null || token.isNull()) {
                continue;
            }
            JsonNode info = token.get("tokenInfo");
            if (info == null || info.isNull()) {
                continue;
            }
            String symbol = text(info.get("symbol"));
            if (symbol.isBlank()) {
                continue;
            }
            String symbolNormalized = symbol.trim().toUpperCase(Locale.ROOT);
            String contract = text(info.get("address"));
            if (!contract.isBlank() && !seen.add(contract.toLowerCase(Locale.ROOT))) {
                continue;
            }

            BigDecimal amount = parseTokenAmount(token, info);
            if (amount == null || amount.signum() == 0) {
                continue;
            }

            BigDecimal priceUsd = extractUsdRate(info.get("price"), symbolNormalized);
            if (priceUsd == null || priceUsd.signum() <= 0) {
                continue;
            }

            BigDecimal valueUsd = amount.multiply(priceUsd).setScale(INTERNAL_USD_SCALE, RoundingMode.HALF_UP);
            totalUsd = totalUsd.add(valueUsd);
            holdings.add(new TokenHolding(contract.isBlank() ? null : contract, symbolNormalized, amount, priceUsd, valueUsd));
        }

        holdings.sort(Comparator.comparing(TokenHolding::valueUsd).reversed());
        List<TokenHolding> top = holdings.size() <= TOP_TOKENS_LIMIT ? holdings : holdings.subList(0, TOP_TOKENS_LIMIT);
        return new ParsedTokens(totalUsd, List.copyOf(top));
    }

    private static BigDecimal parseTokenAmount(JsonNode token, JsonNode info) {
        int decimals = parseInt(text(info.get("decimals")), 0);
        String raw = text(token.get("rawBalance"));
        if (raw.isBlank()) {
            raw = text(token.get("balance"));
        }
        if (raw.isBlank()) {
            return null;
        }
        BigDecimal rawBalance;
        try {
            rawBalance = new BigDecimal(raw);
        } catch (Exception ignored) {
            return null;
        }
        if (decimals <= 0) {
            return rawBalance;
        }
        return rawBalance.movePointLeft(decimals);
    }

    private static BigDecimal extractUsdRate(JsonNode priceNode, String symbolNormalized) {
        if (STABLECOINS.contains(symbolNormalized)) {
            BigDecimal fromApi = rateFromNode(priceNode);
            return fromApi == null ? BigDecimal.ONE : fromApi;
        }
        return rateFromNode(priceNode);
    }

    private static BigDecimal rateFromNode(JsonNode priceNode) {
        if (priceNode == null || priceNode.isNull() || !priceNode.isObject()) {
            return null;
        }
        JsonNode rateNode = priceNode.get("rate");
        if (rateNode == null || rateNode.isNull()) {
            return null;
        }
        if (rateNode.isNumber()) {
            return rateNode.decimalValue();
        }
        String raw = text(rateNode);
        if (raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (Exception ignored) {
            return null;
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

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText("");
        }
        if (node.isNumber()) {
            return node.asText();
        }
        return node.toString();
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    record ParsedTokens(BigDecimal totalUsd, List<TokenHolding> topTokens) {
    }
}

