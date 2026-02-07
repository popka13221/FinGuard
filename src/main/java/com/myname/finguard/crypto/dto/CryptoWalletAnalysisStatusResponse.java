package com.myname.finguard.crypto.dto;

import java.time.Instant;

public record CryptoWalletAnalysisStatusResponse(
        String status,
        int progressPct,
        String stage,
        Instant startedAt,
        Instant updatedAt,
        Instant finishedAt,
        boolean partialReady
) {
}
