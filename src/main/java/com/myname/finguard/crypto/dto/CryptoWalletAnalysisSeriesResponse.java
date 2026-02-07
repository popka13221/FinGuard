package com.myname.finguard.crypto.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CryptoWalletAnalysisSeriesResponse(
        String baseCurrency,
        String window,
        List<SeriesPoint> points,
        Instant asOf,
        boolean synthetic
) {
    public record SeriesPoint(
            Instant at,
            BigDecimal valueInBase
    ) {
    }
}
