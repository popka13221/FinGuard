package com.myname.finguard.accounts.service;

import com.myname.finguard.accounts.dto.CreateAccountRequest;
import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, CurrencyService currencyService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.currencyService = currencyService;
    }

    public UserBalanceResponse getUserBalance(Long userId) {
        if (userId == null) {
            throw unauthorized();
        }
        List<Account> accounts = accountRepository.findByUserId(userId);
        List<UserBalanceResponse.AccountBalance> accountBalances = accounts.stream()
                .map(this::toAccountBalance)
                .toList();

        Map<String, BigDecimal> totals = accounts.stream()
                .filter(account -> !account.isArchived())
                .collect(Collectors.groupingBy(
                        Account::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO,
                                account -> safeAmount(account.getCurrentBalance()),
                                BigDecimal::add)
                ));

        List<UserBalanceResponse.CurrencyBalance> totalsByCurrency = totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new UserBalanceResponse.CurrencyBalance(entry.getKey(), entry.getValue()))
                .toList();

        return new UserBalanceResponse(accountBalances, totalsByCurrency);
    }

    public UserBalanceResponse.AccountBalance createAccount(Long userId, CreateAccountRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isBlank()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Account name is required", HttpStatus.BAD_REQUEST);
        }
        if (name.length() > 255) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Account name must be at most 255 characters", HttpStatus.BAD_REQUEST);
        }

        String currency = currencyService.normalize(request.currency());
        if (!currencyService.isSupported(currency)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported currency", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        BigDecimal initial = safeAmount(request.initialBalance());
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setCurrency(currency);
        account.setInitialBalance(initial);
        account.setCurrentBalance(initial);
        account.setArchived(false);

        Account saved = accountRepository.save(account);
        return toAccountBalance(saved);
    }

    private UserBalanceResponse.AccountBalance toAccountBalance(Account account) {
        return new UserBalanceResponse.AccountBalance(
                account.getId(),
                account.getName(),
                account.getCurrency(),
                safeAmount(account.getCurrentBalance()),
                account.isArchived()
        );
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return Objects.requireNonNullElse(amount, BigDecimal.ZERO);
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}
