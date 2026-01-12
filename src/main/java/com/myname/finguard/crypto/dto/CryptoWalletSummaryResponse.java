package com.myname.finguard.crypto.dto;

import java.math.BigDecimal;
import java.util.List;

public record CryptoWalletSummaryResponse(
        List<CryptoWalletDto> wallets,
        BigDecimal totalValueInBase,
        String baseCurrency
) {
}

