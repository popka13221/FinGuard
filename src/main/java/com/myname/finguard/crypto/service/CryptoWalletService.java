package com.myname.finguard.crypto.service;

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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CryptoWalletService {

    private static final Pattern ETH_ADDRESS = Pattern.compile("^(0x)?[0-9a-fA-F]{40}$");
    private static final Pattern BTC_BASE58 = Pattern.compile("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$");
    private static final Pattern BTC_BECH32 = Pattern.compile("^(?i)bc1[0-9a-z]{11,71}$");

    private final CryptoWalletRepository cryptoWalletRepository;
    private final UserRepository userRepository;
    private final CryptoWalletBalanceService walletBalanceService;
    private final CryptoRatesService cryptoRatesService;

    public CryptoWalletService(
            CryptoWalletRepository cryptoWalletRepository,
            UserRepository userRepository,
            CryptoWalletBalanceService walletBalanceService,
            CryptoRatesService cryptoRatesService
    ) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.userRepository = userRepository;
        this.walletBalanceService = walletBalanceService;
        this.cryptoRatesService = cryptoRatesService;
    }

    public CryptoWalletDto createWallet(Long userId, CreateCryptoWalletRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        CryptoNetwork network = parseNetwork(request.network());
        String address = normalizeOriginal(request.address());
        String addressNormalized = normalizeAddress(network, address);
        String label = normalizeOptionalLabel(request.label());

        if (cryptoWalletRepository.existsByUserIdAndNetworkAndAddressNormalized(userId, network, addressNormalized)) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Wallet already exists", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        CryptoWallet wallet = new CryptoWallet();
        wallet.setUser(user);
        wallet.setNetwork(network);
        wallet.setAddress(address);
        wallet.setAddressNormalized(addressNormalized);
        wallet.setLabel(label);
        wallet.setArchived(false);

        CryptoWallet saved = cryptoWalletRepository.save(wallet);
        return new CryptoWalletDto(saved.getId(), saved.getNetwork().name(), saved.getLabel(), saved.getAddress(), null, null, user.getBaseCurrency(), null);
    }

    public List<CryptoWalletDto> listWallets(Long userId) {
        if (userId == null) {
            throw unauthorized();
        }
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        String baseCurrency = normalizeCurrency(user.getBaseCurrency());
        Map<String, BigDecimal> prices = fetchPrices(baseCurrency);

        List<CryptoWallet> wallets = cryptoWalletRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(userId);
        if (wallets.isEmpty()) {
            return List.of();
        }

        return wallets.stream()
                .map(wallet -> toDto(wallet, baseCurrency, prices))
                .toList();
    }

    public void archiveWallet(Long userId, Long walletId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (walletId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Wallet id is required", HttpStatus.BAD_REQUEST);
        }
        CryptoWallet wallet = cryptoWalletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Wallet not found", HttpStatus.BAD_REQUEST));
        if (!wallet.isArchived()) {
            wallet.setArchived(true);
            cryptoWalletRepository.save(wallet);
        }
    }

    private CryptoWalletDto toDto(CryptoWallet wallet, String baseCurrency, Map<String, BigDecimal> prices) {
        CryptoWalletBalanceProvider.WalletBalance balance = null;
        try {
            balance = walletBalanceService.latestBalance(wallet.getNetwork(), wallet.getAddressNormalized());
        } catch (Exception ignored) {
            // ignore balance failures for individual wallets
        }

        BigDecimal balanceValue = balance == null ? null : balance.balance();
        BigDecimal valueInBase = computeValueInBase(wallet.getNetwork(), balanceValue, baseCurrency, prices);
        return new CryptoWalletDto(
                wallet.getId(),
                wallet.getNetwork().name(),
                wallet.getLabel(),
                wallet.getAddress(),
                balanceValue,
                valueInBase,
                baseCurrency,
                balance == null ? null : balance.asOf()
        );
    }

    private Map<String, BigDecimal> fetchPrices(String baseCurrency) {
        try {
            CryptoRatesProvider.CryptoRates rates = cryptoRatesService.latestRates(baseCurrency);
            if (rates == null || rates.rates() == null) {
                return Collections.emptyMap();
            }
            return rates.rates().stream()
                    .filter(rate -> rate != null && rate.code() != null && rate.price() != null)
                    .collect(Collectors.toMap(rate -> rate.code().toUpperCase(), CryptoRatesProvider.CryptoRate::price, (a, b) -> a));
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private BigDecimal computeValueInBase(
            CryptoNetwork network,
            BigDecimal balance,
            String baseCurrency,
            Map<String, BigDecimal> prices
    ) {
        if (network == null || balance == null || baseCurrency == null || baseCurrency.isBlank() || prices.isEmpty()) {
            return null;
        }
        BigDecimal price = prices.get(network.name());
        if (price == null) {
            return null;
        }
        int scale = isCrypto(baseCurrency) ? 8 : 2;
        return balance.multiply(price).setScale(scale, RoundingMode.HALF_UP);
    }

    private boolean isCrypto(String currency) {
        String normalized = normalizeCurrency(currency);
        return "BTC".equalsIgnoreCase(normalized) || "ETH".equalsIgnoreCase(normalized);
    }

    private CryptoNetwork parseNetwork(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase();
        try {
            return CryptoNetwork.valueOf(normalized);
        } catch (Exception e) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Unsupported network", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeOriginal(String address) {
        if (address == null) {
            return "";
        }
        return address.trim();
    }

    private String normalizeOptionalLabel(String label) {
        if (label == null) {
            return null;
        }
        String trimmed = label.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeCurrency(String code) {
        if (code == null || code.isBlank()) {
            return "USD";
        }
        return code.trim().toUpperCase();
    }

    private String normalizeAddress(CryptoNetwork network, String address) {
        if (address == null || address.isBlank()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Wallet address is required", HttpStatus.BAD_REQUEST);
        }
        String trimmed = address.trim();
        return switch (network) {
            case ETH -> normalizeEthAddress(trimmed);
            case BTC -> normalizeBtcAddress(trimmed);
        };
    }

    private String normalizeEthAddress(String address) {
        if (!ETH_ADDRESS.matcher(address).matches()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Invalid ETH address", HttpStatus.BAD_REQUEST);
        }
        String hex = address.startsWith("0x") || address.startsWith("0X")
                ? address.substring(2)
                : address;
        return "0x" + hex.toLowerCase();
    }

    private String normalizeBtcAddress(String address) {
        if (BTC_BECH32.matcher(address).matches()) {
            return address.toLowerCase();
        }
        if (BTC_BASE58.matcher(address).matches()) {
            return address;
        }
        throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Invalid BTC address", HttpStatus.BAD_REQUEST);
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}
