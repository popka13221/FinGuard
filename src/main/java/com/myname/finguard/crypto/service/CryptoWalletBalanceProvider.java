package com.myname.finguard.crypto.service;

import com.myname.finguard.crypto.model.CryptoNetwork;
import java.math.BigDecimal;
import java.time.Instant;

public interface CryptoWalletBalanceProvider {
    WalletBalance fetchLatest(CryptoNetwork network, String addressNormalized);

    record WalletBalance(CryptoNetwork network, String address, BigDecimal balance, Instant asOf) {
    }
}

