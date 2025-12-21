package com.myname.finguard.accounts.service;

import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
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

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
