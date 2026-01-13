package com.myname.finguard.reports.service;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.CryptoRatesService;
import com.myname.finguard.reports.dto.CashFlowResponse;
import com.myname.finguard.reports.dto.ReportPeriod;
import com.myname.finguard.reports.dto.ReportSummaryResponse;
import com.myname.finguard.reports.dto.ReportsByCategoryResponse;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReportsService {

    private static final Duration PERIOD_WEEK = Duration.ofDays(7);
    private static final Duration PERIOD_MONTH = Duration.ofDays(30);

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;
    private final CryptoRatesService cryptoRatesService;

    public ReportsService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            CurrencyService currencyService,
            CryptoRatesService cryptoRatesService
    ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.currencyService = currencyService;
        this.cryptoRatesService = cryptoRatesService;
    }

    public ReportSummaryResponse summary(Long userId, ReportPeriod period, Instant from, Instant to) {
        if (userId == null) {
            throw unauthorized();
        }
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        String baseCurrency = normalizeCurrency(user.getBaseCurrency());

        DateRange range = resolveRange(period == null ? ReportPeriod.MONTH : period, from, to);
        var rows = transactionRepository.sumByTypeAndCurrency(userId, range.from(), range.to());

        Collection<String> currencies = rows.stream().map(TransactionRepository.TypeCurrencyTotal::getCurrency).toList();
        ConversionContext ctx = conversionContext(baseCurrency, currencies);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (var row : rows) {
            TransactionType type = row.getType();
            if (type == null) {
                continue;
            }
            BigDecimal amountInBase = convertToBase(row.getTotal(), row.getCurrency(), ctx);
            if (TransactionType.INCOME == type) {
                income = income.add(amountInBase);
            } else if (TransactionType.EXPENSE == type) {
                expense = expense.add(amountInBase);
            }
        }

        int scale = ctx.scale();
        income = income.setScale(scale, RoundingMode.HALF_UP);
        expense = expense.setScale(scale, RoundingMode.HALF_UP);
        BigDecimal net = income.subtract(expense).setScale(scale, RoundingMode.HALF_UP);

        return new ReportSummaryResponse(ctx.baseCurrency(), range.from(), range.to(), income, expense, net);
    }

    public ReportsByCategoryResponse byCategory(Long userId, ReportPeriod period, Instant from, Instant to, int limit) {
        if (userId == null) {
            throw unauthorized();
        }
        if (limit <= 0 || limit > 100) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "`limit` must be between 1 and 100", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        String baseCurrency = normalizeCurrency(user.getBaseCurrency());

        DateRange range = resolveRange(period == null ? ReportPeriod.MONTH : period, from, to);
        var rows = transactionRepository.sumByCategoryTypeAndCurrency(userId, range.from(), range.to());

        Collection<String> currencies = rows.stream().map(TransactionRepository.CategoryTypeCurrencyTotal::getCurrency).toList();
        ConversionContext ctx = conversionContext(baseCurrency, currencies);

        record Key(Long categoryId, TransactionType type) {
        }

        Map<Key, BigDecimal> totals = new HashMap<>();
        Map<Key, String> names = new HashMap<>();

        for (var row : rows) {
            if (row.getCategoryId() == null || row.getType() == null) {
                continue;
            }
            BigDecimal amountInBase = convertToBase(row.getTotal(), row.getCurrency(), ctx);
            Key key = new Key(row.getCategoryId(), row.getType());
            totals.merge(key, amountInBase, BigDecimal::add);
            if (row.getCategoryName() != null && !row.getCategoryName().isBlank()) {
                names.putIfAbsent(key, row.getCategoryName());
            }
        }

        Comparator<ReportsByCategoryResponse.CategoryTotal> byTotalDesc = Comparator
                .comparing(ReportsByCategoryResponse.CategoryTotal::total, Comparator.nullsLast(BigDecimal::compareTo))
                .reversed();

        var expenses = totals.entrySet().stream()
                .filter(entry -> TransactionType.EXPENSE == entry.getKey().type())
                .map(entry -> new ReportsByCategoryResponse.CategoryTotal(
                        entry.getKey().categoryId(),
                        names.getOrDefault(entry.getKey(), ""),
                        entry.getValue().setScale(ctx.scale(), RoundingMode.HALF_UP)
                ))
                .sorted(byTotalDesc)
                .limit(limit)
                .toList();

        var incomes = totals.entrySet().stream()
                .filter(entry -> TransactionType.INCOME == entry.getKey().type())
                .map(entry -> new ReportsByCategoryResponse.CategoryTotal(
                        entry.getKey().categoryId(),
                        names.getOrDefault(entry.getKey(), ""),
                        entry.getValue().setScale(ctx.scale(), RoundingMode.HALF_UP)
                ))
                .sorted(byTotalDesc)
                .limit(limit)
                .toList();

        return new ReportsByCategoryResponse(ctx.baseCurrency(), range.from(), range.to(), expenses, incomes);
    }

    public CashFlowResponse cashFlow(Long userId, Instant from, Instant to) {
        if (userId == null) {
            throw unauthorized();
        }
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        String baseCurrency = normalizeCurrency(user.getBaseCurrency());

        DateRange range = resolveRange(ReportPeriod.MONTH, from, to);
        var rows = transactionRepository.findCashFlowRows(userId, range.from(), range.to());

        Collection<String> currencies = rows.stream().map(TransactionRepository.CashFlowRow::getCurrency).toList();
        ConversionContext ctx = conversionContext(baseCurrency, currencies);

        record DayTotals(BigDecimal income, BigDecimal expense) {
            DayTotals addIncome(BigDecimal delta) {
                return new DayTotals(income.add(delta), expense);
            }

            DayTotals addExpense(BigDecimal delta) {
                return new DayTotals(income, expense.add(delta));
            }
        }

        Map<LocalDate, DayTotals> byDay = new TreeMap<>();
        for (var row : rows) {
            if (row.getTransactionDate() == null || row.getType() == null) {
                continue;
            }
            LocalDate day = LocalDate.ofInstant(row.getTransactionDate(), ZoneOffset.UTC);
            BigDecimal amountInBase = convertToBase(row.getAmount(), row.getCurrency(), ctx);
            DayTotals current = byDay.getOrDefault(day, new DayTotals(BigDecimal.ZERO, BigDecimal.ZERO));
            if (TransactionType.INCOME == row.getType()) {
                byDay.put(day, current.addIncome(amountInBase));
            } else if (TransactionType.EXPENSE == row.getType()) {
                byDay.put(day, current.addExpense(amountInBase));
            }
        }

        var points = byDay.entrySet().stream()
                .map(entry -> {
                    DayTotals totals = entry.getValue();
                    BigDecimal income = totals.income().setScale(ctx.scale(), RoundingMode.HALF_UP);
                    BigDecimal expense = totals.expense().setScale(ctx.scale(), RoundingMode.HALF_UP);
                    BigDecimal net = income.subtract(expense).setScale(ctx.scale(), RoundingMode.HALF_UP);
                    return new CashFlowResponse.CashFlowPoint(entry.getKey(), income, expense, net);
                })
                .toList();

        return new CashFlowResponse(ctx.baseCurrency(), range.from(), range.to(), points);
    }

    private DateRange resolveRange(ReportPeriod period, Instant from, Instant to) {
        Duration duration = periodDuration(period);
        Instant now = Instant.now();
        Instant end = to != null ? to : now;

        Instant start;
        if (from != null) {
            start = from;
        } else if (to != null) {
            start = end.minus(duration);
        } else {
            start = now.minus(duration);
        }

        if (end.isBefore(start)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "`to` must be after `from`", HttpStatus.BAD_REQUEST);
        }

        return new DateRange(start, end);
    }

    private Duration periodDuration(ReportPeriod period) {
        if (period == null) {
            return PERIOD_MONTH;
        }
        return switch (period) {
            case WEEK -> PERIOD_WEEK;
            case MONTH -> PERIOD_MONTH;
        };
    }

    private ConversionContext conversionContext(String baseCurrency, Collection<String> currencies) {
        String base = normalizeCurrency(baseCurrency);
        if (base.isBlank() || !currencyService.isSupported(base)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported base currency", HttpStatus.BAD_REQUEST);
        }

        boolean needCrypto = isCrypto(base) || currencies.stream().anyMatch(this::isCrypto);
        boolean needFx = needsFxRates(base, currencies);

        Map<String, BigDecimal> fxUsd = needFx ? currencyService.latestRates("USD").rates() : Map.of();
        Map<String, BigDecimal> cryptoUsd = needCrypto ? fetchCryptoUsdPrices() : Map.of();

        int scale = isCrypto(base) ? 8 : 2;
        return new ConversionContext(base, scale, fxUsd, cryptoUsd);
    }

    private boolean needsFxRates(String baseCurrency, Collection<String> currencies) {
        if (!isCrypto(baseCurrency) && !"USD".equalsIgnoreCase(baseCurrency)) {
            return true;
        }
        for (String c : currencies) {
            if (c == null || c.isBlank()) {
                continue;
            }
            String normalized = normalizeCurrency(c);
            if (!isCrypto(normalized) && !"USD".equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal convertToBase(BigDecimal amount, String currency, ConversionContext ctx) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        String from = normalizeCurrency(currency);
        if (from.isBlank() || !currencyService.isSupported(from)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported currency: " + from, HttpStatus.BAD_REQUEST);
        }
        if (from.equalsIgnoreCase(ctx.baseCurrency())) {
            return amount.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        BigDecimal usd = toUsd(amount, from, ctx);
        return fromUsd(usd, ctx);
    }

    private BigDecimal toUsd(BigDecimal amount, String currency, ConversionContext ctx) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if ("USD".equalsIgnoreCase(currency)) {
            return amount;
        }
        if (isCrypto(currency)) {
            BigDecimal usdPrice = requireCryptoUsdPrice(ctx.cryptoUsdPrices(), currency);
            return amount.multiply(usdPrice);
        }

        BigDecimal rate = requireFxUsdRate(ctx.usdFxRates(), currency);
        return amount.divide(rate, 12, RoundingMode.HALF_UP);
    }

    private BigDecimal fromUsd(BigDecimal usdAmount, ConversionContext ctx) {
        if (usdAmount == null) {
            return BigDecimal.ZERO.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        String base = ctx.baseCurrency();
        if ("USD".equalsIgnoreCase(base)) {
            return usdAmount.setScale(ctx.scale(), RoundingMode.HALF_UP);
        }
        if (isCrypto(base)) {
            BigDecimal baseUsdPrice = requireCryptoUsdPrice(ctx.cryptoUsdPrices(), base);
            return usdAmount.divide(baseUsdPrice, ctx.scale(), RoundingMode.HALF_UP);
        }
        BigDecimal baseRate = requireFxUsdRate(ctx.usdFxRates(), base);
        return usdAmount.multiply(baseRate).setScale(ctx.scale(), RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> fetchCryptoUsdPrices() {
        CryptoRatesProvider.CryptoRates rates = cryptoRatesService.latestRates("USD");
        if (rates == null || rates.rates() == null) {
            return Map.of();
        }
        Map<String, BigDecimal> prices = new HashMap<>();
        for (var item : rates.rates()) {
            if (item == null || item.code() == null || item.price() == null) {
                continue;
            }
            prices.putIfAbsent(item.code().trim().toUpperCase(Locale.ROOT), item.price());
        }
        return prices;
    }

    private BigDecimal requireFxUsdRate(Map<String, BigDecimal> usdFxRates, String currency) {
        if (usdFxRates == null || usdFxRates.isEmpty()) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rates are not available", HttpStatus.SERVICE_UNAVAILABLE);
        }
        BigDecimal rate = usdFxRates.get(currency);
        if (rate == null || rate.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "FX rate is not available for currency: " + currency, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return rate;
    }

    private BigDecimal requireCryptoUsdPrice(Map<String, BigDecimal> cryptoUsdPrices, String currency) {
        if (cryptoUsdPrices == null || cryptoUsdPrices.isEmpty()) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto rates are not available", HttpStatus.SERVICE_UNAVAILABLE);
        }
        BigDecimal price = cryptoUsdPrices.get(currency);
        if (price == null || price.signum() == 0) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Crypto price is not available for currency: " + currency, HttpStatus.SERVICE_UNAVAILABLE);
        }
        return price;
    }

    private boolean isCrypto(String currency) {
        String normalized = normalizeCurrency(currency);
        return "BTC".equalsIgnoreCase(normalized) || "ETH".equalsIgnoreCase(normalized);
    }

    private String normalizeCurrency(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }

    private record DateRange(Instant from, Instant to) {
        DateRange {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }
    }

    private record ConversionContext(
            String baseCurrency,
            int scale,
            Map<String, BigDecimal> usdFxRates,
            Map<String, BigDecimal> cryptoUsdPrices
    ) {
        ConversionContext {
            Objects.requireNonNull(baseCurrency, "baseCurrency");
            Objects.requireNonNull(usdFxRates, "usdFxRates");
            Objects.requireNonNull(cryptoUsdPrices, "cryptoUsdPrices");
        }
    }
}

