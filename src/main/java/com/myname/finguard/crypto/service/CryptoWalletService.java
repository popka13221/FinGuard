package com.myname.finguard.crypto.service;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.CryptoRatesService;
import com.myname.finguard.dashboard.events.UserDataChangedEvent;
import com.myname.finguard.crypto.dto.CreateCryptoWalletRequest;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.dto.CryptoWalletSummaryResponse;
import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.model.CryptoWallet;
import com.myname.finguard.crypto.repository.CryptoWalletRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CryptoWalletService {

    private static final Logger log = LoggerFactory.getLogger(CryptoWalletService.class);
    private static final Pattern ETH_ADDRESS = Pattern.compile("^(0x)?[0-9a-fA-F]{40}$");
    private static final Pattern BTC_BASE58 = Pattern.compile("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$");
    private static final Pattern BTC_BECH32 = Pattern.compile("^(?i)bc1[0-9a-z]{11,71}$");

    private final CryptoWalletRepository cryptoWalletRepository;
    private final UserRepository userRepository;
    private final CryptoWalletBalanceService walletBalanceService;
    private final CryptoRatesService cryptoRatesService;
    private final CurrencyService currencyService;
    private final EthWalletPortfolioService ethWalletPortfolioService;
    private final ArbitrumWalletPortfolioService arbitrumWalletPortfolioService;
    private final CryptoWalletAnalysisService cryptoWalletAnalysisService;
    private final int maxWalletsPerUser;
    @Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;

    public CryptoWalletService(
            CryptoWalletRepository cryptoWalletRepository,
            UserRepository userRepository,
            CryptoWalletBalanceService walletBalanceService,
            CryptoRatesService cryptoRatesService,
            CurrencyService currencyService,
            EthWalletPortfolioService ethWalletPortfolioService,
            ArbitrumWalletPortfolioService arbitrumWalletPortfolioService,
            CryptoWalletAnalysisService cryptoWalletAnalysisService,
            @Value("${app.crypto.wallet.max-per-user:25}") int maxWalletsPerUser
    ) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.userRepository = userRepository;
        this.walletBalanceService = walletBalanceService;
        this.cryptoRatesService = cryptoRatesService;
        this.currencyService = currencyService;
        this.ethWalletPortfolioService = ethWalletPortfolioService;
        this.arbitrumWalletPortfolioService = arbitrumWalletPortfolioService;
        this.cryptoWalletAnalysisService = cryptoWalletAnalysisService;
        this.maxWalletsPerUser = Math.max(0, maxWalletsPerUser);
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

        CryptoWallet existing = cryptoWalletRepository.findByUserIdAndNetworkAndAddressNormalized(userId, network, addressNormalized)
                .orElse(null);
        if (existing != null) {
            if (!existing.isArchived()) {
                throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Wallet already exists", HttpStatus.BAD_REQUEST);
            }
            // Clean up previously removed wallets to allow re-adding the same address.
            cryptoWalletRepository.delete(existing);
        }

        if (maxWalletsPerUser > 0 && cryptoWalletRepository.countByUserIdAndArchivedFalse(userId) >= maxWalletsPerUser) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Wallet limit reached", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        CryptoWallet wallet = new CryptoWallet();
        wallet.setUser(user);
        wallet.setNetwork(network);
        wallet.setAddress(address);
        wallet.setAddressNormalized(addressNormalized);
        wallet.setLabel(label);
        wallet.setArchived(false);

        try {
            CryptoWallet saved = cryptoWalletRepository.save(wallet);
            try {
                if (cryptoWalletAnalysisService != null) {
                    cryptoWalletAnalysisService.enqueueInitialAnalysis(saved);
                }
            } catch (Exception ex) {
                log.debug("Failed to enqueue wallet analysis for walletId={}: {}", saved.getId(), ex.getMessage());
            }
            publishUserDataChanged(userId);
            return new CryptoWalletDto(saved.getId(), saved.getNetwork().name(), saved.getLabel(), saved.getAddress(), null, null, user.getBaseCurrency(), null);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Wallet already exists", HttpStatus.BAD_REQUEST);
        }
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

    public CryptoWalletSummaryResponse walletsSummary(Long userId) {
        if (userId == null) {
            throw unauthorized();
        }
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        String baseCurrency = normalizeCurrency(user.getBaseCurrency());
        Map<String, BigDecimal> prices = fetchPrices(baseCurrency);

        List<CryptoWallet> wallets = cryptoWalletRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(userId);
        if (wallets.isEmpty()) {
            return new CryptoWalletSummaryResponse(List.of(), BigDecimal.ZERO, baseCurrency);
        }

        List<CryptoWalletDto> dtos = wallets.stream()
                .map(wallet -> toDto(wallet, baseCurrency, prices))
                .toList();

        BigDecimal total = null;
        boolean hasUnknown = dtos.stream().anyMatch(dto -> dto == null || dto.valueInBase() == null);
        if (!hasUnknown) {
            total = dtos.stream()
                    .map(CryptoWalletDto::valueInBase)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return new CryptoWalletSummaryResponse(dtos, total, baseCurrency);
    }

    public void deleteWallet(Long userId, Long walletId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (walletId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Wallet id is required", HttpStatus.BAD_REQUEST);
        }
        CryptoWallet wallet = cryptoWalletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Wallet not found", HttpStatus.BAD_REQUEST));
        log.info("Wallet deleted: userId={}, walletId={}, network={}", userId, walletId, wallet.getNetwork());
        cryptoWalletRepository.delete(wallet);
        publishUserDataChanged(userId);
    }

    public CryptoWalletDto updateWalletLabel(Long userId, Long walletId, String label) {
        if (userId == null) {
            throw unauthorized();
        }
        if (walletId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Wallet id is required", HttpStatus.BAD_REQUEST);
        }
        CryptoWallet wallet = cryptoWalletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Wallet not found", HttpStatus.BAD_REQUEST));
        String previousLabel = wallet.getLabel();
        String normalizedLabel = normalizeOptionalLabel(label);
        wallet.setLabel(normalizedLabel);
        CryptoWallet saved = cryptoWalletRepository.save(wallet);
        log.info(
                "Wallet label updated: userId={}, walletId={}, hadLabel={}, hasLabel={}",
                userId,
                walletId,
                previousLabel != null && !previousLabel.isBlank(),
                normalizedLabel != null && !normalizedLabel.isBlank()
        );
        publishUserDataChanged(userId);

        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        String baseCurrency = normalizeCurrency(user.getBaseCurrency());
        Map<String, BigDecimal> prices = fetchPrices(baseCurrency);
        return toDto(saved, baseCurrency, prices);
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
        if (wallet.getNetwork() == CryptoNetwork.ETH) {
            BigDecimal tokenValueInBase = computeEthTokenValueInBase(wallet.getAddressNormalized(), baseCurrency);
            valueInBase = mergeValues(valueInBase, tokenValueInBase, baseCurrency);
        }
        if (wallet.getNetwork() == CryptoNetwork.ARBITRUM) {
            BigDecimal tokenValueInBase = computeArbitrumTokenValueInBase(wallet.getAddressNormalized(), baseCurrency);
            valueInBase = mergeValues(valueInBase, tokenValueInBase, baseCurrency);
        }
        if (wallet.getNetwork() == CryptoNetwork.EVM) {
            BigDecimal ethTokens = computeEthTokenValueInBase(wallet.getAddressNormalized(), baseCurrency);
            BigDecimal arbTokens = computeArbitrumTokenValueInBase(wallet.getAddressNormalized(), baseCurrency);
            valueInBase = mergeValues(valueInBase, ethTokens, baseCurrency);
            valueInBase = mergeValues(valueInBase, arbTokens, baseCurrency);
        }
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

    private BigDecimal computeEthTokenValueInBase(String addressNormalized, String baseCurrency) {
        if (ethWalletPortfolioService == null || addressNormalized == null || addressNormalized.isBlank()) {
            return null;
        }
        try {
            EthWalletPortfolioProvider.EthWalletPortfolio portfolio = ethWalletPortfolioService.latestPortfolio(addressNormalized);
            BigDecimal tokenValueUsd = portfolio == null ? null : portfolio.tokenValueUsd();
            return convertUsdToBase(tokenValueUsd, baseCurrency);
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal computeArbitrumTokenValueInBase(String addressNormalized, String baseCurrency) {
        if (arbitrumWalletPortfolioService == null || addressNormalized == null || addressNormalized.isBlank()) {
            return null;
        }
        try {
            ArbitrumWalletPortfolioProvider.ArbitrumWalletPortfolio portfolio = arbitrumWalletPortfolioService.latestPortfolio(addressNormalized);
            BigDecimal tokenValueUsd = portfolio == null ? null : portfolio.tokenValueUsd();
            return convertUsdToBase(tokenValueUsd, baseCurrency);
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal mergeValues(BigDecimal nativeValue, BigDecimal tokenValue, String baseCurrency) {
        if (nativeValue == null && tokenValue == null) {
            return null;
        }
        if (nativeValue == null) {
            return tokenValue;
        }
        if (tokenValue == null) {
            return nativeValue;
        }
        int scale = isCrypto(baseCurrency) ? 8 : 2;
        return nativeValue.add(tokenValue).setScale(scale, RoundingMode.HALF_UP);
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

    private BigDecimal convertUsdToBase(BigDecimal usdAmount, String baseCurrency) {
        if (usdAmount == null || baseCurrency == null || baseCurrency.isBlank()) {
            return null;
        }
        String base = normalizeCurrency(baseCurrency);
        int scale = isCrypto(base) ? 8 : 2;
        if ("USD".equalsIgnoreCase(base)) {
            return usdAmount.setScale(scale, RoundingMode.HALF_UP);
        }
        if (isCrypto(base)) {
            Map<String, BigDecimal> usdPrices = fetchPrices("USD");
            BigDecimal baseUsdPrice = usdPrices.get(base);
            if (baseUsdPrice == null || baseUsdPrice.signum() == 0) {
                return null;
            }
            return usdAmount.divide(baseUsdPrice, scale, RoundingMode.HALF_UP);
        }
        if (currencyService == null) {
            return null;
        }
        try {
            BigDecimal rate = Objects.requireNonNull(currencyService.latestRates("USD")).rates().get(base);
            if (rate == null) {
                return null;
            }
            return usdAmount.multiply(rate).setScale(scale, RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return null;
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
        BigDecimal price = prices.get(priceCode(network));
        if (price == null) {
            return null;
        }
        int scale = isCrypto(baseCurrency) ? 8 : 2;
        return balance.multiply(price).setScale(scale, RoundingMode.HALF_UP);
    }

    private String priceCode(CryptoNetwork network) {
        if (network == null) {
            return null;
        }
        return switch (network) {
            case BTC -> "BTC";
            case ETH -> "ETH";
            case ARBITRUM -> "ETH";
            case EVM -> "ETH";
        };
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
            case ARBITRUM -> normalizeEthAddress(trimmed);
            case EVM -> normalizeEthAddress(trimmed);
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

    private void publishUserDataChanged(Long userId) {
        if (eventPublisher == null || userId == null) {
            return;
        }
        eventPublisher.publishEvent(new UserDataChangedEvent(userId));
    }
}
