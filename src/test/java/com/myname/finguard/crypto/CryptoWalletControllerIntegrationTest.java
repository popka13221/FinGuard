package com.myname.finguard.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import com.myname.finguard.crypto.service.CryptoWalletBalanceProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CryptoWalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoWalletRepository cryptoWalletRepository;

    @MockBean
    private CryptoWalletBalanceProvider walletBalanceProvider;

    @MockBean
    private CryptoRatesProvider cryptoRatesProvider;

    @BeforeEach
    void setupUser() {
        cryptoWalletRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setBaseCurrency("USD");
        userRepository.save(user);
    }

    @AfterEach
    void cleanup() {
        cryptoWalletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createListAndArchiveWallet() throws Exception {
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

        String listResponse = mockMvc.perform(get("/api/crypto/wallets"))
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

        long id = wallet.get("id").asLong();
        mockMvc.perform(delete("/api/crypto/wallets/{id}", id))
                .andExpect(status().isNoContent());

        String listAfterArchive = mockMvc.perform(get("/api/crypto/wallets"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode after = objectMapper.readTree(listAfterArchive);
        assertThat(after.isArray()).isTrue();
        assertThat(after.size()).isEqualTo(0);
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
    @WithMockUser(username = "user@example.com")
    void rejectsInvalidEthAddress() throws Exception {
        String response = mockMvc.perform(post("/api/crypto/wallets")
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
    @WithMockUser(username = "user@example.com")
    void rejectsDuplicateWallet() throws Exception {
        String btcAddress = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh";
        mockMvc.perform(post("/api/crypto/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"%s","label":"Ledger"}
                                """.formatted(btcAddress)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/crypto/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"network":"BTC","address":"%s","label":"Ledger"}
                                """.formatted(btcAddress)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void cannotArchiveOtherUsersWallet() throws Exception {
        User other = new User();
        other.setEmail("other@example.com");
        other.setPasswordHash("hash");
        other.setBaseCurrency("USD");
        userRepository.save(other);

        User owner = userRepository.findByEmail("user@example.com").orElseThrow();
        CryptoWallet wallet = new CryptoWallet();
        wallet.setUser(owner);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Owner");
        wallet.setArchived(false);
        cryptoWalletRepository.save(wallet);

        String response = mockMvc.perform(delete("/api/crypto/wallets/{id}", wallet.getId()))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode error = objectMapper.readTree(response);
        assertThat(error.get("code").asText()).isEqualTo(ErrorCodes.BAD_REQUEST);
    }
}
