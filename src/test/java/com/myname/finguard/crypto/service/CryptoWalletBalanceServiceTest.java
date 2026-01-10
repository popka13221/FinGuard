package com.myname.finguard.crypto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.crypto.model.CryptoNetwork;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CryptoWalletBalanceServiceTest {

    @Test
    void cachesSuccessfulBalancesWithinTtl() {
        AtomicInteger calls = new AtomicInteger();
        CryptoWalletBalanceProvider provider = (network, address) -> {
            calls.incrementAndGet();
            return new CryptoWalletBalanceProvider.WalletBalance(network, address, new BigDecimal("0.5"), Instant.EPOCH);
        };
        CryptoWalletBalanceService service = new CryptoWalletBalanceService(provider, Duration.ofMinutes(10), 1000, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        CryptoWalletBalanceProvider.WalletBalance first = service.latestBalance(CryptoNetwork.BTC, "addr");
        CryptoWalletBalanceProvider.WalletBalance second = service.latestBalance(CryptoNetwork.BTC, "addr");

        assertThat(calls.get()).isEqualTo(1);
        assertThat(second).isSameAs(first);
        assertThat(second.balance()).isEqualByComparingTo("0.5");
    }

    @Test
    void returnsCachedBalanceWhenProviderFails() {
        AtomicInteger calls = new AtomicInteger();
        CryptoWalletBalanceProvider provider = (network, address) -> {
            int attempt = calls.incrementAndGet();
            if (attempt == 1) {
                return new CryptoWalletBalanceProvider.WalletBalance(network, address, new BigDecimal("1.25"), Instant.EPOCH);
            }
            throw new RuntimeException("boom");
        };
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        CryptoWalletBalanceService service = new CryptoWalletBalanceService(provider, Duration.ZERO, 1000, Clock.fixed(now, ZoneOffset.UTC));

        CryptoWalletBalanceProvider.WalletBalance first = service.latestBalance(CryptoNetwork.ETH, "addr");
        CryptoWalletBalanceProvider.WalletBalance second = service.latestBalance(CryptoNetwork.ETH, "addr");

        assertThat(calls.get()).isEqualTo(2);
        assertThat(second).isSameAs(first);
        assertThat(second.balance()).isEqualByComparingTo("1.25");
    }

    @Test
    void failsWhenProviderMissingAndNoCache() {
        CryptoWalletBalanceService service = new CryptoWalletBalanceService(null, Duration.ofMinutes(10), 1000, Clock.systemUTC());

        assertThatThrownBy(() -> service.latestBalance(CryptoNetwork.BTC, "addr"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.INTERNAL_ERROR);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Test
    void rejectsMissingNetworkOrAddress() {
        CryptoWalletBalanceService service = new CryptoWalletBalanceService((n, a) -> null, Duration.ofSeconds(10), 1000, Clock.systemUTC());

        assertThatThrownBy(() -> service.latestBalance(null, "addr"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.BAD_REQUEST);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        assertThatThrownBy(() -> service.latestBalance(CryptoNetwork.BTC, " "))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.BAD_REQUEST);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}
