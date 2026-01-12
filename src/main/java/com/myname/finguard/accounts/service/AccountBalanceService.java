package com.myname.finguard.accounts.service;

import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountBalanceService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountBalanceService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account recalculateAndPersist(Long userId, Long accountId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (accountId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Account id is required", HttpStatus.BAD_REQUEST);
        }
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Account not found", HttpStatus.BAD_REQUEST));

        BigDecimal net = Objects.requireNonNullElse(transactionRepository.sumNetByUserIdAndAccountId(userId, accountId), BigDecimal.ZERO);
        BigDecimal initial = Objects.requireNonNullElse(account.getInitialBalance(), BigDecimal.ZERO);
        account.setCurrentBalance(initial.add(net));
        return accountRepository.save(account);
    }

    public long countTransactions(Long userId, Long accountId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (accountId == null) {
            return 0;
        }
        return transactionRepository.countByAccountIdAndUserId(accountId, userId);
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}
