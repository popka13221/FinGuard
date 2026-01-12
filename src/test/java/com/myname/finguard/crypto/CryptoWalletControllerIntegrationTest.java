package com.myname.finguard.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.MailService;
import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import com.myname.finguard.crypto.service.CryptoWalletBalanceProvider;
import com.myname.finguard.crypto.service.ArbitrumWalletPortfolioProvider;
import com.myname.finguard.crypto.service.EthWalletPortfolioProvider;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
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
        "app.crypto.cache-ttl-seconds=0",
        "app.crypto.wallet.cache-ttl-seconds=0",
        "app.crypto.wallet.eth.portfolio.cache-ttl-seconds=0",
        "app.crypto.wallet.arbitrum.portfolio.cache-ttl-seconds=0"
})
class CryptoWalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CryptoWalletRepository cryptoWalletRepository;

    @Autowired
    private MailService mailService;

    @MockBean
    private CryptoWalletBalanceProvider walletBalanceProvider;

    @MockBean
    private CryptoRatesProvider cryptoRatesProvider;

    @MockBean
    private EthWalletPortfolioProvider ethWalletPortfolioProvider;

    @MockBean
    private ArbitrumWalletPortfolioProvider arbitrumWalletPortfolioProvider;

    @BeforeEach
    void setup() {
        mailService.clearOutbox();
    }

    @Test
    @Transactional
    void createListDeleteAndReAddWallet() throws Exception {
        String email = "wallet-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");
        String btcAddress = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh";
        when(cryptoRatesProvider.fetchLatest("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(
                                new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", new BigDecimal("65000"), BigDecimal.ZERO, List.of()),
                                new CryptoRatesProvider.CryptoRate("ETH", "Ethereum", new BigDecimal("3000"), BigDecimal.ZERO, List.of())
                        )
                ));
        when(walletBalanceProvider.fetchLatest(CryptoNetwork.BTC, btcAddress))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.BTC,
                        btcAddress,
                        new BigDecimal("0.12345678"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));

        String createResponse = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"%s","label":"Ledger"}
                                """.formatted(btcAddress)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        assertThat(created.get("id").asLong()).isPositive();

        String listResponse = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(listResponse);
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isEqualTo(1);
        JsonNode wallet = list.get(0);
        assertThat(wallet.get("network").asText()).isEqualTo("BTC");
        assertThat(wallet.get("label").asText()).isEqualTo("Ledger");
        assertThat(wallet.get("balance").decimalValue()).isEqualByComparingTo("0.12345678");
        assertThat(wallet.get("valueInBase").decimalValue()).isEqualByComparingTo("8024.69");
        assertThat(wallet.get("baseCurrency").asText()).isEqualTo("USD");

        String summaryResponse = mockMvc.perform(get("/api/crypto/wallets/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode summary = objectMapper.readTree(summaryResponse);
        assertThat(summary.get("wallets").isArray()).isTrue();
        assertThat(summary.get("wallets").size()).isEqualTo(1);
        assertThat(summary.get("totalValueInBase").decimalValue()).isEqualByComparingTo("8024.69");
        assertThat(summary.get("baseCurrency").asText()).isEqualTo("USD");

        long id = wallet.get("id").asLong();
        mockMvc.perform(delete("/api/crypto/wallets/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(cryptoWalletRepository.findById(id)).isEmpty();

        String listAfterDelete = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode after = objectMapper.readTree(listAfterDelete);
        assertThat(after.isArray()).isTrue();
        assertThat(after.size()).isEqualTo(0);

        String summaryAfterDeleteResponse = mockMvc.perform(get("/api/crypto/wallets/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode summaryAfterDelete = objectMapper.readTree(summaryAfterDeleteResponse);
        assertThat(summaryAfterDelete.get("wallets").size()).isEqualTo(0);
        assertThat(summaryAfterDelete.get("totalValueInBase").decimalValue()).isEqualByComparingTo("0");

        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"%s","label":"Ledger"}
                                """.formatted(btcAddress)))
                .andExpect(status().isCreated());

        String listAfterReAdd = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode afterReAdd = objectMapper.readTree(listAfterReAdd);
        assertThat(afterReAdd.isArray()).isTrue();
        assertThat(afterReAdd.size()).isEqualTo(1);
    }

    @Test
    void anonymousIsForbidden() throws Exception {
        mockMvc.perform(get("/api/crypto/wallets"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/crypto/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh","label":"Ledger"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void rejectsInvalidEthAddress() throws Exception {
        String token = registerVerifyAndLogin("bad-eth-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String response = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ETH","address":"0x123","label":"Test"}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
    }

    @Test
    @Transactional
    void rejectsInvalidBtcAddress() throws Exception {
        String token = registerVerifyAndLogin("bad-btc-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String response = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"not-an-address","label":"Test"}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
    }

    @Test
    @Transactional
    void rejectsNetworkThatFailsBeanValidation() throws Exception {
        String token = registerVerifyAndLogin("bad-network-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String response = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"B1","address":"bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh","label":"Test"}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
        assertThat(error.get("message").asText()).contains("Network must be a 2-16 letter code");
    }

    @Test
    @Transactional
    void rejectsDuplicateWalletDueToEthNormalization() throws Exception {
        String token = registerVerifyAndLogin("dup-eth-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String ethAddress = "0xAbCdEfAbCdEfAbCdEfAbCdEfAbCdEfAbCdEfAbCd";
        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ETH","address":"%s","label":"Ledger"}
                                """.formatted(ethAddress)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ETH","address":"%s","label":"Ledger"}
                                """.formatted(ethAddress.toLowerCase())))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
    }

    @Test
    @Transactional
    void includesEthTokenValueInValueInBase() throws Exception {
        String email = "wallet-eth-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");
        String ethAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";

        when(cryptoRatesProvider.fetchLatest("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("ETH", "Ethereum", new BigDecimal("3000"), BigDecimal.ZERO, List.of()))
                ));
        when(walletBalanceProvider.fetchLatest(CryptoNetwork.ETH, ethAddress))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.ETH,
                        ethAddress,
                        new BigDecimal("0.00029"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));
        when(ethWalletPortfolioProvider.fetchLatest(ethAddress))
                .thenReturn(new EthWalletPortfolioProvider.EthWalletPortfolio(
                        ethAddress,
                        Instant.parse("2024-01-01T00:00:00Z"),
                        new BigDecimal("1000.00"),
                        List.of()
                ));

        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ETH","address":"%s","label":"MetaMask"}
                                """.formatted(ethAddress)))
                .andExpect(status().isCreated());

        String listResponse = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(listResponse);
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isEqualTo(1);
        JsonNode wallet = list.get(0);
        assertThat(wallet.get("network").asText()).isEqualTo("ETH");
        assertThat(wallet.get("valueInBase").decimalValue()).isEqualByComparingTo("1000.87");
    }

    @Test
    @Transactional
    void includesArbitrumTokenValueInValueInBase() throws Exception {
        String email = "wallet-arb-" + UUID.randomUUID() + "@example.com";
        String token = registerVerifyAndLogin(email, "StrongPass1!", "USD");
        String address = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";

        when(cryptoRatesProvider.fetchLatest("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("ETH", "Ethereum", new BigDecimal("3000"), BigDecimal.ZERO, List.of()))
                ));
        when(walletBalanceProvider.fetchLatest(CryptoNetwork.ARBITRUM, address))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.ARBITRUM,
                        address,
                        new BigDecimal("1.5"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));
        when(arbitrumWalletPortfolioProvider.fetchLatest(address))
                .thenReturn(new ArbitrumWalletPortfolioProvider.ArbitrumWalletPortfolio(
                        address,
                        Instant.parse("2024-01-01T00:00:00Z"),
                        new BigDecimal("500.00"),
                        List.of()
                ));

        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"ARBITRUM","address":"%s","label":"Arbitrum"}
                                """.formatted(address)))
                .andExpect(status().isCreated());

        String listResponse = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(listResponse);
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isEqualTo(1);
        JsonNode wallet = list.get(0);
        assertThat(wallet.get("network").asText()).isEqualTo("ARBITRUM");
        assertThat(wallet.get("valueInBase").decimalValue()).isEqualByComparingTo("5000.00");
    }

    @Test
    @Transactional
    void listsWalletsInCryptoBaseCurrencyWith8DecimalsAndUsesNormalizedAddressForLookup() throws Exception {
        String token = registerVerifyAndLogin("base-btc-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "BTC");
        String original = "BC1QXY2KGDYGJRSQTZQ2N0YRF2493P83KKFJHX0WLH";
        String normalized = original.toLowerCase();

        when(cryptoRatesProvider.fetchLatest("BTC"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "BTC",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", BigDecimal.ONE, BigDecimal.ZERO, List.of()))
                ));
        when(walletBalanceProvider.fetchLatest(CryptoNetwork.BTC, normalized))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.BTC,
                        normalized,
                        new BigDecimal("0.1"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));

        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"%s","label":"Ledger"}
                                """.formatted(original)))
                .andExpect(status().isCreated());

        String listResponse = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(listResponse);
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isEqualTo(1);
        JsonNode wallet = list.get(0);
        assertThat(wallet.get("baseCurrency").asText()).isEqualTo("BTC");
        assertThat(wallet.get("valueInBase").decimalValue()).isEqualByComparingTo("0.10000000");

        verify(walletBalanceProvider).fetchLatest(CryptoNetwork.BTC, normalized);
    }

    @Test
    @Transactional
    void cannotArchiveOtherUsersWallet() throws Exception {
        String ownerToken = registerVerifyAndLogin("owner-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String otherToken = registerVerifyAndLogin("other-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        String createResponse = mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh","label":"Owner"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long walletId = objectMapper.readTree(createResponse).get("id").asLong();

        String response = mockMvc.perform(delete("/api/crypto/wallets/{id}", walletId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.BAD_REQUEST);
    }

    @Test
    @Transactional
    void listWalletsIsScopedToUser() throws Exception {
        String ownerToken = registerVerifyAndLogin("owner-list-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");
        String otherToken = registerVerifyAndLogin("other-list-" + UUID.randomUUID() + "@example.com", "StrongPass1!", "USD");

        mockMvc.perform(post("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh","label":"Owner"}
                                """))
                .andExpect(status().isCreated());

        String otherList = mockMvc.perform(get("/api/crypto/wallets")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(otherList);
        assertThat(list.isArray()).isTrue();
        assertThat(list).isEmpty();
    }

    private String registerVerifyAndLogin(String email, String password, String baseCurrency) throws Exception {
        String registerPayload = """
                {
                  "email": "%s",
                  "password": "%s",
                  "fullName": "Test User",
                  "baseCurrency": "%s"
                }
                """.formatted(email, password, baseCurrency);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        MailService.MailMessage msg = mailService.getOutbox().get(mailService.getOutbox().size() - 1);
        String verifyToken = extractCode(msg.body());
        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s"}
                                """.formatted(email, verifyToken)))
                .andExpect(status().isOk());

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginNode = objectMapper.readTree(loginResponse);
        return loginNode.get("token").asText();
    }

    private String extractCode(String body) {
        if (body == null) {
            return "";
        }
        Matcher tokenParam = Pattern.compile("token=([^&\\s]+)").matcher(body);
        if (tokenParam.find()) {
            return URLDecoder.decode(tokenParam.group(1), java.nio.charset.StandardCharsets.UTF_8);
        }
        Matcher codeLine = Pattern.compile("(?i)code:\\s*([A-Za-z0-9-]{6,})").matcher(body);
        if (codeLine.find()) {
            return codeLine.group(1).trim();
        }
        Matcher digits = Pattern.compile("\\b\\d{6}\\b").matcher(body);
        if (digits.find()) {
            return digits.group().trim();
        }
        Matcher hex = Pattern.compile("\\b(?=[0-9a-fA-F-]*\\d)[0-9a-fA-F-]{6,}\\b").matcher(body);
        return hex.find() ? hex.group().trim() : "";
    }
}
