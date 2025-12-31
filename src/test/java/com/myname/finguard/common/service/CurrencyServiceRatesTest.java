package com.myname.finguard.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CurrencyServiceRatesTest {

    @Test
    void cachesSuccessfulRatesWithinTtl() {
        AtomicInteger calls = new AtomicInteger();
        FxRatesProvider provider = base -> {
            calls.incrementAndGet();
            return new FxRatesProvider.FxRates(base, Instant.EPOCH, Map.of("EUR", new BigDecimal("0.9")));
        };
        CurrencyService service = new CurrencyService(provider, Duration.ofMinutes(10), Clock.systemUTC());

        FxRatesProvider.FxRates first = service.latestRates("USD");
        FxRatesProvider.FxRates second = service.latestRates("USD");

        assertThat(calls.get()).isEqualTo(1);
        assertThat(second).isSameAs(first);
        assertThat(second.rates().get("EUR")).isEqualByComparingTo("0.9");
    }

    @Test
    void returnsCachedRatesWhenProviderFails() {
        AtomicInteger calls = new AtomicInteger();
        FxRatesProvider provider = base -> {
            int attempt = calls.incrementAndGet();
            if (attempt == 1) {
                return new FxRatesProvider.FxRates(base, Instant.EPOCH, Map.of("EUR", new BigDecimal("0.9")));
            }
            throw new RuntimeException("boom");
        };
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        CurrencyService service = new CurrencyService(provider, Duration.ZERO, Clock.fixed(now, ZoneOffset.UTC));

        FxRatesProvider.FxRates first = service.latestRates("USD");
        FxRatesProvider.FxRates second = service.latestRates("USD");

        assertThat(calls.get()).isEqualTo(2);
        assertThat(second).isSameAs(first);
    }

    @Test
    void rejectsUnsupportedBaseCurrency() {
        FxRatesProvider provider = base -> new FxRatesProvider.FxRates(base, Instant.EPOCH, Map.of("USD", BigDecimal.ONE));
        CurrencyService service = new CurrencyService(provider, Duration.ofMinutes(10), Clock.systemUTC());

        assertThatThrownBy(() -> service.latestRates("ABC"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.BAD_REQUEST);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void failsWhenProviderMissingAndNoCache() {
        CurrencyService service = new CurrencyService(null, Duration.ofMinutes(10), Clock.systemUTC());

        assertThatThrownBy(() -> service.latestRates("USD"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.INTERNAL_ERROR);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }
}
