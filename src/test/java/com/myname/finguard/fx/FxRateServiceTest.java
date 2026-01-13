package com.myname.finguard.fx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.FxRatesProvider;
import com.myname.finguard.fx.model.FxRate;
import com.myname.finguard.fx.repository.FxRateRepository;
import com.myname.finguard.fx.service.FxRateService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FxRateServiceTest {

    @Mock
    private FxRateRepository fxRateRepository;

    @Mock
    private CurrencyService currencyService;

    private FxRateService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new FxRateService(fxRateRepository, currencyService);
    }

    @Test
    void latestStoredRatesValidatesBase() {
        assertThatThrownBy(() -> service.latestStoredRates(" "))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Base currency is required");
    }

    @Test
    void latestStoredRatesReturnsNullWhenNothingStored() {
        when(fxRateRepository.findLatestAsOf("USD")).thenReturn(null);
        assertThat(service.latestStoredRates("USD")).isNull();
    }

    @Test
    void latestStoredRatesFiltersNullsAndDuplicates() {
        Instant asOf = Instant.parse("2024-01-01T00:00:00Z");
        when(fxRateRepository.findLatestAsOf("USD")).thenReturn(asOf);

        FxRate eur = new FxRate();
        eur.setBaseCurrency("USD");
        eur.setQuoteCurrency("eur");
        eur.setRate(new BigDecimal("0.9"));
        eur.setAsOf(asOf);

        FxRate eurDup = new FxRate();
        eurDup.setBaseCurrency("USD");
        eurDup.setQuoteCurrency("EUR");
        eurDup.setRate(new BigDecimal("0.91"));
        eurDup.setAsOf(asOf);

        FxRate broken = new FxRate();
        broken.setBaseCurrency("USD");
        broken.setQuoteCurrency(null);
        broken.setRate(null);
        broken.setAsOf(asOf);

        when(fxRateRepository.findByBaseCurrencyAndAsOf("USD", asOf)).thenReturn(List.of(broken, eur, eurDup));

        FxRatesProvider.FxRates rates = service.latestStoredRates("usd");

        assertThat(rates).isNotNull();
        assertThat(rates.baseCurrency()).isEqualTo("USD");
        assertThat(rates.asOf()).isEqualTo(asOf);
        assertThat(rates.rates()).containsEntry("EUR", new BigDecimal("0.9"));
    }

    @Test
    void upsertSnapshotValidatesInputs() {
        Instant asOf = Instant.parse("2024-01-01T00:00:00Z");
        when(currencyService.isSupported("USD")).thenReturn(true);

        assertThatThrownBy(() -> service.upsertSnapshot(" ", asOf, Map.of("EUR", new BigDecimal("0.9"))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Base currency is required");

        assertThatThrownBy(() -> service.upsertSnapshot("BTC", asOf, Map.of("USD", new BigDecimal("1"))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Unsupported base currency");

        assertThatThrownBy(() -> service.upsertSnapshot("USD", null, Map.of("EUR", new BigDecimal("0.9"))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("`asOf` is required");

        assertThatThrownBy(() -> service.upsertSnapshot("USD", asOf, Map.of()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("`rates` must not be empty");
    }

    @Test
    void upsertSnapshotRejectsUnsupportedQuoteOrInvalidRates() {
        Instant asOf = Instant.parse("2024-01-01T00:00:00Z");
        when(currencyService.isSupported("USD")).thenReturn(true);
        when(currencyService.isSupported("EUR")).thenReturn(true);
        when(currencyService.isSupported("RUB")).thenReturn(false);

        assertThatThrownBy(() -> service.upsertSnapshot("USD", asOf, Map.of("EUR", BigDecimal.ZERO)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Rate must be positive");

        assertThatThrownBy(() -> service.upsertSnapshot("USD", asOf, Map.of("RUB", new BigDecimal("90"))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Unsupported quote currency");
    }

    @Test
    void upsertSnapshotStoresNormalizedRatesAndEvictsCache() {
        Instant asOf = Instant.parse("2024-01-01T00:00:00Z");
        when(currencyService.isSupported("USD")).thenReturn(true);
        when(currencyService.isSupported("EUR")).thenReturn(true);
        when(currencyService.isSupported("CNY")).thenReturn(true);

        when(fxRateRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        FxRatesProvider.FxRates stored = service.upsertSnapshot("usd", asOf, Map.of(
                "eur", new BigDecimal("0.9"),
                "USD", new BigDecimal("1.0"),
                "CNY", new BigDecimal("7.2")
        ));

        assertThat(stored.baseCurrency()).isEqualTo("USD");
        assertThat(stored.asOf()).isEqualTo(asOf);
        assertThat(stored.rates()).containsOnlyKeys("EUR", "CNY");
        assertThat(stored.rates().get("EUR")).isEqualByComparingTo("0.9");
        assertThat(stored.rates().get("CNY")).isEqualByComparingTo("7.2");

        verify(fxRateRepository).deleteByBaseCurrencyAndAsOf("USD", asOf);
        ArgumentCaptor<List<FxRate>> captor = ArgumentCaptor.forClass(List.class);
        verify(fxRateRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        verify(currencyService).evictRatesCache("USD");
    }

    @Test
    void latestRateOrNullReturnsNullForBlankOrMissing() {
        assertThat(service.latestRateOrNull(" ", "EUR")).isNull();
        assertThat(service.latestRateOrNull("USD", " ")).isNull();

        when(fxRateRepository.findTopByBaseCurrencyAndQuoteCurrencyOrderByAsOfDesc("USD", "EUR"))
                .thenReturn(Optional.empty());
        assertThat(service.latestRateOrNull("USD", "EUR")).isNull();
    }

    @Test
    void latestRateOrNullReturnsStoredRate() {
        FxRate rate = new FxRate();
        rate.setBaseCurrency("USD");
        rate.setQuoteCurrency("EUR");
        rate.setRate(new BigDecimal("0.9"));
        rate.setAsOf(Instant.now());
        when(fxRateRepository.findTopByBaseCurrencyAndQuoteCurrencyOrderByAsOfDesc("USD", "EUR"))
                .thenReturn(Optional.of(rate));

        assertThat(service.latestRateOrNull("usd", "eur")).isEqualByComparingTo("0.9");
        verify(fxRateRepository).findTopByBaseCurrencyAndQuoteCurrencyOrderByAsOfDesc("USD", "EUR");
    }
}

