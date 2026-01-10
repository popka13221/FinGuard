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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpEthplorerWalletPortfolioProvider implements EthWalletPortfolioProvider {

    private static final String PROVIDER_KEY = "ethplorer";

    private static final int TOP_TOKENS_LIMIT = 5;
    private static final int INTERNAL_USD_SCALE = 12;
    private static final int DEFAULT_MAX_TOKENS_SCANNED = 200;
    private static final int MAX_TOKEN_DECIMALS = 30;
    private static final int MAX_RAW_BALANCE_LENGTH = 120;

    private static final Pattern CONTRACT_ADDRESS = Pattern.compile("^0x[0-9a-f]{40}$");

    // Mainnet stablecoin contracts only; do NOT use symbol-based fallbacks (easy to spoof).
    private static final Set<String> STABLECOIN_CONTRACTS = Set.of(
            "0xdac17f958d2ee523a2206206994597c13d831ec7", // USDT
            "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48", // USDC
            "0x6b175474e89094c44da98b954eedeac495271d0f", // DAI
            "0x4fabb145d64652a948d72533023f6e7a623c7c53", // BUSD
            "0x0000000000085d4780b73119b644ae5ecd22b376", // TUSD
            "0x8e870d67f660d95d5be530380d0ec0bd388289e1", // USDP
            "0x056fd409e1d7a124bd7017459dfea2f387b6d5cd"  // GUSD
    );

    private final RestClient restClient;
    private final String apiKey;
    private final int maxTokensScanned;
    private final com.myname.finguard.common.service.ExternalProviderGuard guard;
    private final int budgetLimit;
    private final long budgetWindowMs;

    @Autowired
    public HttpEthplorerWalletPortfolioProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.wallet.eth.portfolio.provider-base-url:https://api.ethplorer.io}") String baseUrl,
            @Value("${app.crypto.wallet.eth.portfolio.api-key:freekey}") String apiKey,
            @Value("${app.crypto.wallet.eth.portfolio.max-tokens-scanned:" + DEFAULT_MAX_TOKENS_SCANNED + "}") int maxTokensScanned,
            Environment environment,
            com.myname.finguard.common.service.ExternalProviderGuard guard,
            @Value("${app.external.providers.budget.ethplorer.limit:60}") int budgetLimit,
            @Value("${app.external.providers.budget.ethplorer.window-ms:60000}") long budgetWindowMs
    ) {
        this.restClient = builder.baseUrl(trimTrailingSlash(baseUrl)).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.maxTokensScanned = Math.max(0, maxTokensScanned);
        this.guard = guard;
        this.budgetLimit = Math.max(0, budgetLimit);
        this.budgetWindowMs = Math.max(0, budgetWindowMs);

        boolean prod = environment != null && environment.matchesProfiles("prod");
        if (prod && (this.apiKey.isBlank() || "freekey".equalsIgnoreCase(this.apiKey))) {
            throw new IllegalStateException("Ethplorer apiKey must be configured for prod profile");
        }
    }

    @Override
    public EthWalletPortfolio fetchLatest(String addressNormalized) {
        if (addressNormalized == null || addressNormalized.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Ethplorer apiKey is not configured");
        }

        JsonNode response = guarded(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getAddressInfo/{address}")
                        .queryParam("apiKey", apiKey)
                        .build(addressNormalized))
                .retrieve()
                .body(JsonNode.class));
        if (response == null || response.isNull()) {
            throw new IllegalStateException("Empty ETH portfolio response");
        }

        Instant asOf = Instant.now();
        ParsedTokens parsed = parseTokens(response, maxTokensScanned);
        return new EthWalletPortfolio(addressNormalized, asOf, parsed.totalUsd(), parsed.topTokens());
    }

    private <T> T guarded(Supplier<T> call) {
        if (guard == null) {
            return call.get();
        }
        return guard.execute(PROVIDER_KEY, budgetLimit, budgetWindowMs, call);
    }

    static ParsedTokens parseTokens(JsonNode response) {
        return parseTokens(response, DEFAULT_MAX_TOKENS_SCANNED);
    }

    static ParsedTokens parseTokens(JsonNode response, int maxTokensScanned) {
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
        int max = Math.max(0, maxTokensScanned);
        if (max == 0) {
            return new ParsedTokens(BigDecimal.ZERO, List.of());
        }

        int scanned = 0;
        for (JsonNode token : tokens) {
            if (scanned++ >= max) {
                break;
            }
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
            String contract = normalizeContract(text(info.get("address")));
            if (contract.isBlank() || !seen.add(contract)) {
                continue;
            }

            BigDecimal amount = parseTokenAmount(token, info);
            if (amount == null || amount.signum() == 0) {
                continue;
            }

            BigDecimal priceUsd = extractUsdRate(info.get("price"), contract);
            if (priceUsd == null || priceUsd.signum() <= 0) {
                continue;
            }

            BigDecimal valueUsd = amount.multiply(priceUsd).setScale(INTERNAL_USD_SCALE, RoundingMode.HALF_UP);
            totalUsd = totalUsd.add(valueUsd);
            holdings.add(new TokenHolding(contract, symbolNormalized, amount, priceUsd, valueUsd));
        }

        holdings.sort(Comparator.comparing(TokenHolding::valueUsd).reversed());
        List<TokenHolding> top = holdings.size() <= TOP_TOKENS_LIMIT ? holdings : holdings.subList(0, TOP_TOKENS_LIMIT);
        return new ParsedTokens(totalUsd, List.copyOf(top));
    }

    private static BigDecimal parseTokenAmount(JsonNode token, JsonNode info) {
        int decimals = parseInt(text(info.get("decimals")), 0);
        if (decimals < 0 || decimals > MAX_TOKEN_DECIMALS) {
            return null;
        }
        String raw = text(token.get("rawBalance"));
        if (raw.isBlank()) {
            raw = text(token.get("balance"));
        }
        if (raw.isBlank()) {
            return null;
        }
        if (raw.length() > MAX_RAW_BALANCE_LENGTH) {
            return null;
        }
        BigDecimal rawBalance;
        try {
            rawBalance = new BigDecimal(raw);
        } catch (Exception ignored) {
            return null;
        }
        if (rawBalance.signum() < 0) {
            return null;
        }
        if (decimals <= 0) {
            return rawBalance;
        }
        return rawBalance.movePointLeft(decimals);
    }

    private static BigDecimal extractUsdRate(JsonNode priceNode, String contractNormalized) {
        BigDecimal fromApi = rateFromNode(priceNode);
        if (fromApi != null) {
            return fromApi;
        }
        if (contractNormalized != null && STABLECOIN_CONTRACTS.contains(contractNormalized)) {
            return BigDecimal.ONE;
        }
        return null;
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

    record ParsedTokens(BigDecimal totalUsd, List<TokenHolding> topTokens) {
    }
}
