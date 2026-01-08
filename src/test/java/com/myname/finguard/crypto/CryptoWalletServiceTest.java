package com.myname.finguard.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.CryptoRatesService;
import com.myname.finguard.crypto.dto.CreateCryptoWalletRequest;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import com.myname.finguard.crypto.service.CryptoWalletBalanceProvider;
import com.myname.finguard.crypto.service.CryptoWalletBalanceService;
import com.myname.finguard.crypto.service.CryptoWalletService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class CryptoWalletServiceTest {

    @Mock
    private CryptoWalletRepository cryptoWalletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CryptoWalletBalanceService walletBalanceService;

    @Mock
    private CryptoRatesService cryptoRatesService;

    private CryptoWalletService cryptoWalletService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        cryptoWalletService = new CryptoWalletService(cryptoWalletRepository, userRepository, walletBalanceService, cryptoRatesService);
    }

    @Test
    void createWalletRejectsUnsupportedNetwork() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> cryptoWalletService.createWallet(1L, new CreateCryptoWalletRequest("SOL", "x", null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Unsupported network");
    }

    @Test
    void createWalletRejectsInvalidEthAddress() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> cryptoWalletService.createWallet(1L, new CreateCryptoWalletRequest("ETH", "0x123", null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid ETH address");
    }

    @Test
    void createWalletNormalizesEthAddressForDeduplication() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoWalletRepository.existsByUserIdAndNetworkAndAddressNormalized(1L, CryptoNetwork.ETH, "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"))
                .thenReturn(false);
        when(cryptoWalletRepository.save(any(CryptoWallet.class))).thenAnswer(invocation -> {
            CryptoWallet saving = invocation.getArgument(0);
            saving.setId(10L);
            return saving;
        });

        CreateCryptoWalletRequest request = new CreateCryptoWalletRequest("eth", "  0xAbCdEfAbCdEfAbCdEfAbCdEfAbCdEfAbCdEfAbCd  ", "  Ledger  ");
        CryptoWalletDto created = cryptoWalletService.createWallet(1L, request);

        assertThat(created.id()).isEqualTo(10L);
        assertThat(created.network()).isEqualTo("ETH");
        assertThat(created.label()).isEqualTo("Ledger");

        ArgumentCaptor<CryptoWallet> captor = ArgumentCaptor.forClass(CryptoWallet.class);
        verify(cryptoWalletRepository).save(captor.capture());
        CryptoWallet saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getNetwork()).isEqualTo(CryptoNetwork.ETH);
        assertThat(saved.getAddress()).isEqualTo("0xAbCdEfAbCdEfAbCdEfAbCdEfAbCdEfAbCdEfAbCd");
        assertThat(saved.getAddressNormalized()).isEqualTo("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd");
        assertThat(saved.getLabel()).isEqualTo("Ledger");
        assertThat(saved.isArchived()).isFalse();
    }

    @Test
    void createWalletRejectsDuplicateAddress() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        CryptoWallet existing = new CryptoWallet();
        existing.setId(10L);
        existing.setUser(user);
        existing.setNetwork(CryptoNetwork.BTC);
        existing.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        existing.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        existing.setArchived(false);
        when(cryptoWalletRepository.findByUserIdAndNetworkAndAddressNormalized(
                1L,
                CryptoNetwork.BTC,
                "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
        )).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> cryptoWalletService.createWallet(1L, new CreateCryptoWalletRequest("BTC", "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Wallet already exists");
    }

    @Test
    void createWalletMapsUniqueConstraintViolationToValidationError() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoWalletRepository.findByUserIdAndNetworkAndAddressNormalized(
                1L,
                CryptoNetwork.BTC,
                "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
        )).thenReturn(Optional.empty());
        when(cryptoWalletRepository.save(any(CryptoWallet.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> cryptoWalletService.createWallet(1L, new CreateCryptoWalletRequest(
                "BTC",
                "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                null
        )))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo(ErrorCodes.VALIDATION_GENERIC);
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void listWalletsReturnsBalancesAndValueInBase() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CryptoWallet wallet = new CryptoWallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Ledger");
        wallet.setArchived(false);

        when(cryptoWalletRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(1L)).thenReturn(List.of(wallet));
        when(walletBalanceService.latestBalance(CryptoNetwork.BTC, wallet.getAddressNormalized()))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.BTC,
                        wallet.getAddressNormalized(),
                        new BigDecimal("0.50"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));
        when(cryptoRatesService.latestRates("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", new BigDecimal("65000"), BigDecimal.ZERO, List.of()))
                ));

        List<CryptoWalletDto> result = cryptoWalletService.listWallets(1L);

        assertThat(result).hasSize(1);
        CryptoWalletDto dto = result.get(0);
        assertThat(dto.network()).isEqualTo("BTC");
        assertThat(dto.label()).isEqualTo("Ledger");
        assertThat(dto.balance()).isEqualByComparingTo("0.50");
        assertThat(dto.valueInBase()).isEqualByComparingTo("32500.00");
        assertThat(dto.baseCurrency()).isEqualTo("USD");
    }

    @Test
    void createWalletTrimsBlankLabelToNull() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoWalletRepository.existsByUserIdAndNetworkAndAddressNormalized(1L, CryptoNetwork.BTC, "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"))
                .thenReturn(false);
        when(cryptoWalletRepository.save(any(CryptoWallet.class))).thenAnswer(invocation -> {
            CryptoWallet saving = invocation.getArgument(0);
            saving.setId(10L);
            return saving;
        });

        cryptoWalletService.createWallet(1L, new CreateCryptoWalletRequest(
                "BTC",
                "  bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh  ",
                "   "
        ));

        ArgumentCaptor<CryptoWallet> captor = ArgumentCaptor.forClass(CryptoWallet.class);
        verify(cryptoWalletRepository).save(captor.capture());
        assertThat(captor.getValue().getLabel()).isNull();
    }

    @Test
    void createWalletNormalizesBtcBech32AddressToLowercaseForDeduplication() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cryptoWalletRepository.existsByUserIdAndNetworkAndAddressNormalized(1L, CryptoNetwork.BTC, "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"))
                .thenReturn(false);
        when(cryptoWalletRepository.save(any(CryptoWallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cryptoWalletService.createWallet(1L, new CreateCryptoWalletRequest(
                "BTC",
                "BC1QXY2KGDYGJRSQTZQ2N0YRF2493P83KKFJHX0WLH",
                "Ledger"
        ));

        ArgumentCaptor<CryptoWallet> captor = ArgumentCaptor.forClass(CryptoWallet.class);
        verify(cryptoWalletRepository).save(captor.capture());
        assertThat(captor.getValue().getAddressNormalized()).isEqualTo("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
    }

    @Test
    void listWalletsDoesNotFailWhenBalanceProviderThrows() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CryptoWallet wallet = new CryptoWallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Ledger");
        wallet.setArchived(false);

        when(cryptoWalletRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(1L)).thenReturn(List.of(wallet));
        when(walletBalanceService.latestBalance(CryptoNetwork.BTC, wallet.getAddressNormalized()))
                .thenThrow(new RuntimeException("boom"));
        when(cryptoRatesService.latestRates("USD"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "USD",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", new BigDecimal("65000"), BigDecimal.ZERO, List.of()))
                ));

        List<CryptoWalletDto> result = cryptoWalletService.listWallets(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).balance()).isNull();
        assertThat(result.get(0).valueInBase()).isNull();
    }

    @Test
    void listWalletsDoesNotFailWhenCryptoRatesServiceThrows() {
        User user = user(1L, "user@example.com", "USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CryptoWallet wallet = new CryptoWallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Ledger");
        wallet.setArchived(false);

        when(cryptoWalletRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(1L)).thenReturn(List.of(wallet));
        when(walletBalanceService.latestBalance(CryptoNetwork.BTC, wallet.getAddressNormalized()))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.BTC,
                        wallet.getAddressNormalized(),
                        new BigDecimal("0.50"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));
        when(cryptoRatesService.latestRates("USD")).thenThrow(new RuntimeException("boom"));

        List<CryptoWalletDto> result = cryptoWalletService.listWallets(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).balance()).isEqualByComparingTo("0.50");
        assertThat(result.get(0).valueInBase()).isNull();
    }

    @Test
    void listWalletsUses8DecimalsWhenBaseCurrencyIsCrypto() {
        User user = user(1L, "user@example.com", "BTC");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CryptoWallet wallet = new CryptoWallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setNetwork(CryptoNetwork.BTC);
        wallet.setAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setAddressNormalized("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh");
        wallet.setLabel("Ledger");
        wallet.setArchived(false);

        when(cryptoWalletRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(1L)).thenReturn(List.of(wallet));
        when(walletBalanceService.latestBalance(CryptoNetwork.BTC, wallet.getAddressNormalized()))
                .thenReturn(new CryptoWalletBalanceProvider.WalletBalance(
                        CryptoNetwork.BTC,
                        wallet.getAddressNormalized(),
                        new BigDecimal("0.1"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ));
        when(cryptoRatesService.latestRates("BTC"))
                .thenReturn(new CryptoRatesProvider.CryptoRates(
                        "BTC",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        List.of(new CryptoRatesProvider.CryptoRate("BTC", "Bitcoin", BigDecimal.ONE, BigDecimal.ZERO, List.of()))
                ));

        List<CryptoWalletDto> result = cryptoWalletService.listWallets(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).baseCurrency()).isEqualTo("BTC");
        assertThat(result.get(0).valueInBase()).isEqualByComparingTo("0.10000000");
    }

    @Test
    void deleteWalletRemovesRecord() {
        CryptoWallet wallet = new CryptoWallet();
        wallet.setId(10L);

        when(cryptoWalletRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(wallet));

        cryptoWalletService.deleteWallet(1L, 10L);

        verify(cryptoWalletRepository).delete(wallet);
    }

    private User user(Long id, String email, String baseCurrency) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setBaseCurrency(baseCurrency);
        return u;
    }
}
