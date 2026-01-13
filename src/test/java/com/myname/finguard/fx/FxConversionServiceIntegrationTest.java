package com.myname.finguard.fx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.FxRatesProvider;
import com.myname.finguard.fx.service.FxConversionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "app.fx.cache-ttl-seconds=0")
class FxConversionServiceIntegrationTest {

    @Autowired
    private FxConversionService fxConversionService;

    @MockBean
    private FxRatesProvider fxRatesProvider;

    @Test
    void convertsViaUsdAnchorRates() {
        when(fxRatesProvider.fetchLatest("USD"))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of(
                                "EUR", new BigDecimal("0.5"),
                                "RUB", new BigDecimal("100.0")
                        )
                ));

        BigDecimal converted = fxConversionService.convert(new BigDecimal("10.00"), "EUR", "RUB");
        assertThat(converted).isEqualByComparingTo("2000.00");
    }

    @Test
    void rejectsCryptoCurrencies() {
        assertThatThrownBy(() -> fxConversionService.convert(new BigDecimal("1.00"), "BTC", "USD"))
                .isInstanceOf(ApiException.class);
    }
}

