package com.myname.finguard.crypto.service;

import com.myname.finguard.crypto.model.CryptoNetwork;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpCryptoWalletBalanceProvider implements CryptoWalletBalanceProvider {

    private static final BigDecimal SATOSHIS_PER_BTC = new BigDecimal("100000000");
    private static final BigDecimal WEI_PER_ETH = new BigDecimal("1000000000000000000");
    private static final int DISPLAY_SCALE = 8;

    private final RestClient btcClient;
    private final RestClient ethClient;
    private final RestClient arbitrumClient;

    public HttpCryptoWalletBalanceProvider(
            RestClient.Builder builder,
            @Value("${app.crypto.wallet.btc.provider-base-url:https://blockstream.info/api}") String btcBaseUrl,
            @Value("${app.crypto.wallet.eth.provider-base-url:https://api.blockcypher.com/v1/eth/main}") String ethBaseUrl,
            @Value("${app.crypto.wallet.arbitrum.provider-base-url:https://arb1.arbitrum.io/rpc}") String arbitrumBaseUrl
    ) {
        this.btcClient = builder.baseUrl(trimTrailingSlash(btcBaseUrl)).build();
        this.ethClient = builder.baseUrl(trimTrailingSlash(ethBaseUrl)).build();
        this.arbitrumClient = builder.baseUrl(trimTrailingSlash(arbitrumBaseUrl)).build();
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
        };
    }

    private WalletBalance fetchBtc(String address, Instant asOf) {
        BlockstreamAddress response = btcClient.get()
                .uri("/address/{address}", address)
                .retrieve()
                .body(BlockstreamAddress.class);
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
        BlockcypherBalance response = ethClient.get()
                .uri("/addrs/{address}/balance", address)
                .retrieve()
                .body(BlockcypherBalance.class);
        if (response == null || response.final_balance() == null) {
            throw new IllegalStateException("Empty ETH balance response");
        }
        BigDecimal wei = response.final_balance();
        BigDecimal eth = wei.divide(WEI_PER_ETH, DISPLAY_SCALE, RoundingMode.DOWN);
        return new WalletBalance(CryptoNetwork.ETH, address, eth, asOf);
    }

    private WalletBalance fetchArbitrum(String address, Instant asOf) {
        JsonRpcResponse response = arbitrumClient.post()
                .body(new JsonRpcRequest("2.0", 1L, "eth_getBalance", List.of(address, "latest")))
                .retrieve()
                .body(JsonRpcResponse.class);
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
