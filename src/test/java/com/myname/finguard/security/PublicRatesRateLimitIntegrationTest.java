package com.myname.finguard.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.FxRatesProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.rate-limit.public-rates.limit=1",
        "app.security.rate-limit.public-rates.window-ms=600000"
})
@Transactional
class PublicRatesRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FxRatesProvider fxRatesProvider;

    @MockBean
    private CryptoRatesProvider cryptoRatesProvider;

    @Test
    void rateLimitsCryptoRatesPerIp() throws Exception {
        org.mockito.Mockito.when(cryptoRatesProvider.fetchLatest(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", BigDecimal.ONE, BigDecimal.ZERO, List.of()))
                ));

        mockMvc.perform(get("/api/crypto/rates").with(req -> {
                    req.setRemoteAddr("203.0.113.10");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/crypto/rates").with(req -> {
                    req.setRemoteAddr("203.0.113.10");
                    return req;
                }))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimitsFxRatesPerIp() throws Exception {
        org.mockito.Mockito.when(fxRatesProvider.fetchLatest(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new FxRatesProvider.FxRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Map.of("EUR", new BigDecimal("0.9"))
                ));

        mockMvc.perform(get("/api/fx/rates").param("base", "USD").with(req -> {
                    req.setRemoteAddr("203.0.113.11");
                    return req;
                }))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/fx/rates").param("base", "USD").with(req -> {
                    req.setRemoteAddr("203.0.113.11");
                    return req;
                }))
                .andExpect(status().isTooManyRequests());
    }
}
