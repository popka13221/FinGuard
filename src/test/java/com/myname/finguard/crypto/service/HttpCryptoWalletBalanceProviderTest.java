package com.myname.finguard.crypto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.myname.finguard.crypto.model.CryptoNetwork;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpCryptoWalletBalanceProviderTest {

    @Test
    void btcConvertsSatoshisToBtcIncludingMempool() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpCryptoWalletBalanceProvider provider = new HttpCryptoWalletBalanceProvider(
                builder,
                "https://btc.example/api/",
                "https://eth.example/v1/eth/main/",
                "https://arb.example/rpc/"
        );

        String address = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh";
        server.expect(requestTo("https://btc.example/api/address/" + address))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "chain_stats": { "funded_txo_sum": 1000, "spent_txo_sum": 200 },
                          "mempool_stats": { "funded_txo_sum": 100, "spent_txo_sum": 50 }
                        }
                        """, MediaType.APPLICATION_JSON));

        CryptoWalletBalanceProvider.WalletBalance balance = provider.fetchLatest(CryptoNetwork.BTC, address);

        assertThat(balance.network()).isEqualTo(CryptoNetwork.BTC);
        assertThat(balance.address()).isEqualTo(address);
        assertThat(balance.balance()).isEqualByComparingTo(new BigDecimal("0.00000850"));
        assertThat(balance.asOf()).isNotNull();
        server.verify();
    }

    @Test
    void ethConvertsWeiToEth() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpCryptoWalletBalanceProvider provider = new HttpCryptoWalletBalanceProvider(
                builder,
                "https://btc.example/api/",
                "https://eth.example/v1/eth/main/",
                "https://arb.example/rpc/"
        );

        String address = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";
        server.expect(requestTo("https://eth.example/v1/eth/main/addrs/" + address + "/balance"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        { "final_balance": 1500000000000000000 }
                        """, MediaType.APPLICATION_JSON));

        CryptoWalletBalanceProvider.WalletBalance balance = provider.fetchLatest(CryptoNetwork.ETH, address);

        assertThat(balance.network()).isEqualTo(CryptoNetwork.ETH);
        assertThat(balance.address()).isEqualTo(address);
        assertThat(balance.balance()).isEqualByComparingTo(new BigDecimal("1.50000000"));
        assertThat(balance.asOf()).isNotNull();
        server.verify();
    }

    @Test
    void arbitrumConvertsWeiToEthUsingJsonRpc() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        HttpCryptoWalletBalanceProvider provider = new HttpCryptoWalletBalanceProvider(
                builder,
                "https://btc.example/api/",
                "https://eth.example/v1/eth/main/",
                "https://arb.example/rpc/"
        );

        String address = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";
        server.expect(requestTo("https://arb.example/rpc"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        { "jsonrpc":"2.0", "id":1, "result":"0x14d1120d7b160000" }
                        """, MediaType.APPLICATION_JSON));

        CryptoWalletBalanceProvider.WalletBalance balance = provider.fetchLatest(CryptoNetwork.ARBITRUM, address);

        assertThat(balance.network()).isEqualTo(CryptoNetwork.ARBITRUM);
        assertThat(balance.address()).isEqualTo(address);
        assertThat(balance.balance()).isEqualByComparingTo(new BigDecimal("1.50000000"));
        assertThat(balance.asOf()).isNotNull();
        server.verify();
    }
}
