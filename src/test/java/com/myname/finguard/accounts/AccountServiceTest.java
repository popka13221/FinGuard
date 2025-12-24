package com.myname.finguard.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.accounts.service.AccountService;
import com.myname.finguard.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void throwsUnauthorizedWhenUserNull() {
        assertThatThrownBy(() -> accountService.getUserBalance(null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User is not authenticated");
    }

    @Test
    void aggregatesTotalsPerCurrencyExcludingArchivedAndNulls() {
        Account usdActive = account(1L, "USD", new BigDecimal("10.50"), false);
        Account usdArchived = account(2L, "USD", new BigDecimal("5.00"), true);
        Account eurActiveNull = account(3L, "EUR", null, false);

        when(accountRepository.findByUserId(42L)).thenReturn(List.of(usdActive, usdArchived, eurActiveNull));

        UserBalanceResponse result = accountService.getUserBalance(42L);

        verify(accountRepository).findByUserId(42L);
        assertThat(result.accounts()).hasSize(3);
        assertThat(result.totalsByCurrency()).containsExactly(
                new UserBalanceResponse.CurrencyBalance("EUR", BigDecimal.ZERO),
                new UserBalanceResponse.CurrencyBalance("USD", new BigDecimal("10.50"))
        );
    }

    private Account account(Long id, String currency, BigDecimal balance, boolean archived) {
        Account a = new Account();
        a.setId(id);
        a.setName("Acc " + id);
        a.setCurrency(currency);
        a.setCurrentBalance(balance);
        a.setArchived(archived);
        return a;
    }
}
