package com.myname.finguard.common.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.external.providers.enabled", havingValue = "true", matchIfMissing = true)
public class HttpFxRatesProvider implements FxRatesProvider {

    private final RestClient restClient;

    public HttpFxRatesProvider(
            RestClient.Builder builder,
            @Value("${app.fx.provider-base-url:https://open.er-api.com/v6/latest}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(trimTrailingSlash(baseUrl)).build();
    }

    @Override
    public FxRates fetchLatest(String baseCurrency) {
        ErApiResponse response = restClient.get()
                .uri("/{base}", baseCurrency)
                .retrieve()
                .body(ErApiResponse.class);
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
