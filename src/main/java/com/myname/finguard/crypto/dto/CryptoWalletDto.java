package com.myname.finguard.crypto.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoWalletDto(
        Long id,
        String network,
        String label,
        String address,
        BigDecimal balance,
        BigDecimal valueInBase,
        String baseCurrency,
        Instant asOf
) {
}

