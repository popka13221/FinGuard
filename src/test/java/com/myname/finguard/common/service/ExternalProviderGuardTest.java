package com.myname.finguard.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class ExternalProviderGuardTest {

    @Test
    void retriesRetryableExceptions() {
        ExternalProviderGuard guard = new ExternalProviderGuard(
                2,
                0,
                0,
                10,
                10_000,
                1000
        );

        AtomicInteger calls = new AtomicInteger();
        String result = guard.execute("test", 100, 60_000, () -> {
            if (calls.incrementAndGet() == 1) {
                throw new RestClientException("boom");
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void budgetExceededFailsFast() {
        ExternalProviderGuard guard = new ExternalProviderGuard(
                1,
                0,
                0,
                10,
                10_000,
                1000
        );

        guard.execute("budget", 1, 60_000, () -> "ok");
        assertThatThrownBy(() -> guard.execute("budget", 1, 60_000, () -> "ok"))
                .isInstanceOf(ExternalProviderGuard.ExternalProviderUnavailableException.class)
                .hasMessageContaining("budget exceeded");
    }

    @Test
    void circuitBreakerOpensAfterFailures() {
        ExternalProviderGuard guard = new ExternalProviderGuard(
                1,
                0,
                0,
                2,
                60_000,
                1000
        );

        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> guard.execute("circuit", 100, 60_000, () -> {
                calls.incrementAndGet();
                throw new RestClientException("down");
            })).isInstanceOf(RestClientException.class);
        }

        assertThatThrownBy(() -> guard.execute("circuit", 100, 60_000, () -> {
            calls.incrementAndGet();
            return "ok";
        }))
                .isInstanceOf(ExternalProviderGuard.ExternalProviderUnavailableException.class)
                .hasMessageContaining("circuit is open");

        assertThat(calls.get()).isEqualTo(2);
    }
}

