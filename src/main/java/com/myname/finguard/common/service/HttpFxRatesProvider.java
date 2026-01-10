package com.myname.finguard.common.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpFxRatesProvider implements FxRatesProvider {

    private static final String PROVIDER_KEY = "fx-erapi";

    private final RestClient restClient;
    private final ExternalProviderGuard guard;
    private final int budgetLimit;
    private final long budgetWindowMs;

    public HttpFxRatesProvider(
            RestClient.Builder builder,
            @Value("${app.fx.provider-base-url:https://open.er-api.com/v6/latest}") String baseUrl
    ) {
        this(builder, baseUrl, null, 0, 0);
    }

    @Autowired
    public HttpFxRatesProvider(
            RestClient.Builder builder,
            @Value("${app.fx.provider-base-url:https://open.er-api.com/v6/latest}") String baseUrl,
            ExternalProviderGuard guard,
            @Value("${app.external.providers.budget.fx.limit:120}") int budgetLimit,
            @Value("${app.external.providers.budget.fx.window-ms:60000}") long budgetWindowMs
    ) {
        this.restClient = builder.baseUrl(trimTrailingSlash(baseUrl)).build();
        this.guard = guard;
        this.budgetLimit = Math.max(0, budgetLimit);
        this.budgetWindowMs = Math.max(0, budgetWindowMs);
    }

    @Override
    public FxRates fetchLatest(String baseCurrency) {
        ErApiResponse response = guarded(() -> restClient.get()
                .uri("/{base}", baseCurrency)
                .retrieve()
                .body(ErApiResponse.class));
        if (response == null || response.rates() == null || response.rates().isEmpty()) {
            throw new IllegalStateException("Empty FX rates response");
        }
        if (response.result() != null && !"success".equalsIgnoreCase(response.result())) {
            throw new IllegalStateException("FX provider error: " + response.result());
        }
        Instant asOf = response.time_last_update_unix() == null
                ? Instant.now()
                : Instant.ofEpochSecond(response.time_last_update_unix());
        return new FxRates(baseCurrency, asOf, response.rates());
    }

    private <T> T guarded(Supplier<T> call) {
        if (guard == null) {
            return call.get();
        }
        return guard.execute(PROVIDER_KEY, budgetLimit, budgetWindowMs, call);
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    private record ErApiResponse(
            String result,
            String base_code,
            Long time_last_update_unix,
            Map<String, BigDecimal> rates
    ) {
    }
}
