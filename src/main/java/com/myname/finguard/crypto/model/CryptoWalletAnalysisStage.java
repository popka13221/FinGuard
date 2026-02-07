package com.myname.finguard.crypto.model;

public enum CryptoWalletAnalysisStage {
    FETCH_TX,
    ENRICH_TX,
    BUILD_SNAPSHOTS,
    DETECT_RECURRING,
    BUILD_INSIGHTS,
    DONE
}
