package com.myname.finguard.crypto.service;

import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.common.util.Redaction;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpCryptoWalletBalanceProvider implements CryptoWalletBalanceProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpCryptoWalletBalanceProvider.class);

    private static final BigDecimal SATOSHIS_PER_BTC = new BigDecimal("100000000");
    private static final BigDecimal WEI_PER_ETH = new BigDecimal("1000000000000000000");
    private static final int DISPLAY_SCALE = 8;

    private static final String PROVIDER_BLOCKSTREAM = "blockstream";
    private static final String PROVIDER_BLOCKCYPHER = "blockcypher";
    private static final String PROVIDER_ARBITRUM_RPC = "arbitrum-rpc";
    private static final String PROVIDER_ETH_RPC = "eth-rpc";

    private final RestClient btcClient;
    private final RestClient ethClient;
    private final RestClient arbitrumClient;
    private final RestClient ethRpcClient;
    private final com.myname.finguard.common.service.ExternalProviderGuard guard;
    private final int blockstreamLimit;
    private final long blockstreamWindowMs;
    private final int blockcypherLimit;
    private final long blockcypherWindowMs;
    private final int arbitrumRpcLimit;
    private final long arbitrumRpcWindowMs;
    private final int ethRpcLimit;
    private final long ethRpcWindowMs;
    private final boolean ethCrossCheckEnabled;
    private final BigDecimal ethCrossCheckAbsThresholdEth;

    public HttpCryptoWalletBalanceProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.wallet.btc.provider-base-url:https://blockstream.info/api}") String btcBaseUrl,
            @Value("${app.crypto.wallet.eth.provider-base-url:https://api.blockcypher.com/v1/eth/main}") String ethBaseUrl,
            @Value("${app.crypto.wallet.arbitrum.provider-base-url:https://arb1.arbitrum.io/rpc}") String arbitrumBaseUrl
    ) {
        this(builder, btcBaseUrl, ethBaseUrl, arbitrumBaseUrl,
                "https://cloudflare-eth.com",
                false,
                new BigDecimal("0.01"),
                null,
                0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Autowired
    public HttpCryptoWalletBalanceProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.wallet.btc.provider-base-url:https://blockstream.info/api}") String btcBaseUrl,
            @Value("${app.crypto.wallet.eth.provider-base-url:https://api.blockcypher.com/v1/eth/main}") String ethBaseUrl,
            @Value("${app.crypto.wallet.arbitrum.provider-base-url:https://arb1.arbitrum.io/rpc}") String arbitrumBaseUrl,
            @Value("${app.crypto.wallet.eth.rpc-base-url:https://cloudflare-eth.com}") String ethRpcBaseUrl,
            @Value("${app.crypto.wallet.eth.cross-check.enabled:false}") boolean ethCrossCheckEnabled,
            @Value("${app.crypto.wallet.eth.cross-check.abs-threshold-eth:0.01}") BigDecimal ethCrossCheckAbsThresholdEth,
            com.myname.finguard.common.service.ExternalProviderGuard guard,
            @Value("${app.external.providers.budget.blockstream.limit:600}") int blockstreamLimit,
            @Value("${app.external.providers.budget.blockstream.window-ms:60000}") long blockstreamWindowMs,
            @Value("${app.external.providers.budget.blockcypher.limit:300}") int blockcypherLimit,
            @Value("${app.external.providers.budget.blockcypher.window-ms:60000}") long blockcypherWindowMs,
            @Value("${app.external.providers.budget.arbitrum-rpc.limit:600}") int arbitrumRpcLimit,
            @Value("${app.external.providers.budget.arbitrum-rpc.window-ms:60000}") long arbitrumRpcWindowMs,
            @Value("${app.external.providers.budget.eth-rpc.limit:300}") int ethRpcLimit,
            @Value("${app.external.providers.budget.eth-rpc.window-ms:60000}") long ethRpcWindowMs
    ) {
        this.btcClient = builder.baseUrl(trimTrailingSlash(btcBaseUrl)).build();
        this.ethClient = builder.baseUrl(trimTrailingSlash(ethBaseUrl)).build();
        this.arbitrumClient = builder.baseUrl(trimTrailingSlash(arbitrumBaseUrl)).build();
        this.ethRpcClient = builder.baseUrl(trimTrailingSlash(ethRpcBaseUrl)).build();
        this.guard = guard;
        this.blockstreamLimit = Math.max(0, blockstreamLimit);
        this.blockstreamWindowMs = Math.max(0, blockstreamWindowMs);
        this.blockcypherLimit = Math.max(0, blockcypherLimit);
        this.blockcypherWindowMs = Math.max(0, blockcypherWindowMs);
        this.arbitrumRpcLimit = Math.max(0, arbitrumRpcLimit);
        this.arbitrumRpcWindowMs = Math.max(0, arbitrumRpcWindowMs);
        this.ethRpcLimit = Math.max(0, ethRpcLimit);
        this.ethRpcWindowMs = Math.max(0, ethRpcWindowMs);
        this.ethCrossCheckEnabled = ethCrossCheckEnabled;
        this.ethCrossCheckAbsThresholdEth = ethCrossCheckAbsThresholdEth == null ? new BigDecimal("0.01") : ethCrossCheckAbsThresholdEth.max(BigDecimal.ZERO);
    }

    @Override
    public WalletBalance fetchLatest(CryptoNetwork network, String addressNormalized) {
        if (network == null) {
            throw new IllegalArgumentException("Network is required");
        }
        if (addressNormalized == null || addressNormalized.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        Instant asOf = Instant.now();
        return switch (network) {
            case BTC -> fetchBtc(addressNormalized, asOf);
            case ETH -> fetchEth(addressNormalized, asOf);
            case ARBITRUM -> fetchArbitrum(addressNormalized, asOf);
            case EVM -> fetchEvm(addressNormalized, asOf);
        };
    }

    private WalletBalance fetchEvm(String address, Instant asOf) {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasAny = false;

        try {
            WalletBalance eth = fetchEth(address, asOf);
            if (eth != null && eth.balance() != null) {
                total = total.add(eth.balance());
                hasAny = true;
            }
        } catch (Exception e) {
            log.debug("EVM balance: ETH fetch failed for address={}", Redaction.maskWalletAddress(address), e);
        }

        try {
            WalletBalance arb = fetchArbitrum(address, asOf);
            if (arb != null && arb.balance() != null) {
                total = total.add(arb.balance());
                hasAny = true;
            }
        } catch (Exception e) {
            log.debug("EVM balance: Arbitrum fetch failed for address={}", Redaction.maskWalletAddress(address), e);
        }

        if (!hasAny) {
            throw new IllegalStateException("Empty EVM balance response");
        }

        return new WalletBalance(CryptoNetwork.EVM, address, total.setScale(DISPLAY_SCALE, RoundingMode.DOWN), asOf);
    }

    private WalletBalance fetchBtc(String address, Instant asOf) {
        BlockstreamAddress response = guarded(PROVIDER_BLOCKSTREAM, blockstreamLimit, blockstreamWindowMs, () -> btcClient.get()
                .uri("/address/{address}", address)
                .retrieve()
                .body(BlockstreamAddress.class));
        if (response == null) {
            throw new IllegalStateException("Empty BTC balance response");
        }
        long confirmed = safeLong(response.chain_stats() == null ? null : response.chain_stats().funded_txo_sum())
                - safeLong(response.chain_stats() == null ? null : response.chain_stats().spent_txo_sum());
        long mempool = safeLong(response.mempool_stats() == null ? null : response.mempool_stats().funded_txo_sum())
                - safeLong(response.mempool_stats() == null ? null : response.mempool_stats().spent_txo_sum());
        BigDecimal satoshis = BigDecimal.valueOf(confirmed + mempool);
        BigDecimal btc = satoshis.divide(SATOSHIS_PER_BTC, DISPLAY_SCALE, RoundingMode.DOWN);
        return new WalletBalance(CryptoNetwork.BTC, address, btc, asOf);
    }

    private WalletBalance fetchEth(String address, Instant asOf) {
        BlockcypherBalance response = guarded(PROVIDER_BLOCKCYPHER, blockcypherLimit, blockcypherWindowMs, () -> ethClient.get()
                .uri("/addrs/{address}/balance", address)
                .retrieve()
                .body(BlockcypherBalance.class));
        if (response == null || response.final_balance() == null) {
            throw new IllegalStateException("Empty ETH balance response");
        }
        BigDecimal wei = response.final_balance();
        BigDecimal eth = wei.divide(WEI_PER_ETH, DISPLAY_SCALE, RoundingMode.DOWN);
        crossCheckEthBalance(address, eth);
        return new WalletBalance(CryptoNetwork.ETH, address, eth, asOf);
    }

    private void crossCheckEthBalance(String address, BigDecimal primaryEth) {
        if (!ethCrossCheckEnabled || primaryEth == null || ethCrossCheckAbsThresholdEth == null) {
            return;
        }
        try {
            BigDecimal secondaryEth = fetchEthRpcBalance(address);
            if (secondaryEth == null) {
                return;
            }
            BigDecimal absDiff = primaryEth.subtract(secondaryEth).abs();
            if (absDiff.compareTo(ethCrossCheckAbsThresholdEth) < 0) {
                return;
            }
            log.warn(
                    "ETH balance cross-check mismatch for address={}, primary={}, secondary={}, absDiff={}",
                    Redaction.maskWalletAddress(address),
                    primaryEth,
                    secondaryEth,
                    absDiff
            );
        } catch (Exception e) {
            log.debug("ETH balance cross-check failed for address={}", Redaction.maskWalletAddress(address), e);
        }
    }

    private BigDecimal fetchEthRpcBalance(String address) {
        JsonRpcResponse response = guarded(PROVIDER_ETH_RPC, ethRpcLimit, ethRpcWindowMs, () -> ethRpcClient.post()
                .body(new JsonRpcRequest("2.0", 1L, "eth_getBalance", List.of(address, "latest")))
                .retrieve()
                .body(JsonRpcResponse.class));
        if (response == null) {
            return null;
        }
        if (response.error() != null && response.error().message() != null && !response.error().message().isBlank()) {
            return null;
        }
        if (response.result() == null || response.result().isBlank()) {
            return null;
        }
        BigDecimal wei = new BigDecimal(parseHexWei(response.result()));
        return wei.divide(WEI_PER_ETH, DISPLAY_SCALE, RoundingMode.DOWN);
    }

    private WalletBalance fetchArbitrum(String address, Instant asOf) {
        JsonRpcResponse response = guarded(PROVIDER_ARBITRUM_RPC, arbitrumRpcLimit, arbitrumRpcWindowMs, () -> arbitrumClient.post()
                .body(new JsonRpcRequest("2.0", 1L, "eth_getBalance", List.of(address, "latest")))
                .retrieve()
                .body(JsonRpcResponse.class));
        if (response == null) {
            throw new IllegalStateException("Empty Arbitrum balance response");
        }
        if (response.error() != null && response.error().message() != null && !response.error().message().isBlank()) {
            throw new IllegalStateException("Arbitrum provider error: " + response.error().message());
        }
        if (response.result() == null || response.result().isBlank()) {
            throw new IllegalStateException("Empty Arbitrum balance result");
        }

        BigDecimal wei = new BigDecimal(parseHexWei(response.result()));
        BigDecimal eth = wei.divide(WEI_PER_ETH, DISPLAY_SCALE, RoundingMode.DOWN);
        return new WalletBalance(CryptoNetwork.ARBITRUM, address, eth, asOf);
    }

    private <T> T guarded(String providerKey, int limit, long windowMs, Supplier<T> call) {
        if (guard == null) {
            return call.get();
        }
        return guard.execute(providerKey, limit, windowMs, call);
    }

    private BigInteger parseHexWei(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigInteger.ZERO;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("0x")) {
            value = value.substring(2);
        }
        if (value.isEmpty()) {
            return BigInteger.ZERO;
        }
        return new BigInteger(value, 16);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }

    private record BlockstreamAddress(Stats chain_stats, Stats mempool_stats) {
    }

    private record Stats(Long funded_txo_sum, Long spent_txo_sum) {
    }

    private record BlockcypherBalance(BigDecimal final_balance) {
    }

    private record JsonRpcRequest(String jsonrpc, long id, String method, List<Object> params) {
    }

    private record JsonRpcResponse(String jsonrpc, Long id, String result, JsonRpcError error) {
    }

    private record JsonRpcError(Integer code, String message) {
    }
}
