package com.myname.finguard.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.accounts.service.AccountBalanceService;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AccountBalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private AccountBalanceService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new AccountBalanceService(accountRepository, transactionRepository);
    }

    @Test
    void recalculateRejectsMissingUser() {
        assertThatThrownBy(() -> service.recalculateAndPersist(null, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User is not authenticated");
    }

    @Test
    void recalculateRejectsMissingAccountId() {
        assertThatThrownBy(() -> service.recalculateAndPersist(1L, null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Account id is required");
    }

    @Test
    void recalculateRejectsWhenAccountNotFound() {
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.recalculateAndPersist(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void recalculateComputesBalanceFromInitialPlusNet() {
        Account account = new Account();
        account.setId(10L);
        account.setInitialBalance(new BigDecimal("100.00"));
        account.setCurrentBalance(new BigDecimal("0.00"));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(transactionRepository.sumNetByUserIdAndAccountId(1L, 10L)).thenReturn(new BigDecimal("12.50"));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account saved = service.recalculateAndPersist(1L, 10L);

        assertThat(saved.getCurrentBalance()).isEqualByComparingTo("112.50");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentBalance()).isEqualByComparingTo("112.50");
    }

    @Test
    void recalculateTreatsNullsAsZero() {
        Account account = new Account();
        account.setId(10L);
        account.setInitialBalance(null);
        account.setCurrentBalance(new BigDecimal("5.00"));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(transactionRepository.sumNetByUserIdAndAccountId(1L, 10L)).thenReturn(null);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account saved = service.recalculateAndPersist(1L, 10L);

        assertThat(saved.getCurrentBalance()).isEqualByComparingTo("0");
    }

    @Test
    void countTransactionsRejectsMissingUserAndHandlesNullAccountId() {
        assertThatThrownBy(() -> service.countTransactions(null, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User is not authenticated");

        assertThat(service.countTransactions(1L, null)).isZero();
    }

    @Test
    void countTransactionsDelegatesToRepository() {
        when(transactionRepository.countByAccountIdAndUserId(10L, 1L)).thenReturn(7L);
        assertThat(service.countTransactions(1L, 10L)).isEqualTo(7L);
        verify(transactionRepository).countByAccountIdAndUserId(10L, 1L);
    }
}

