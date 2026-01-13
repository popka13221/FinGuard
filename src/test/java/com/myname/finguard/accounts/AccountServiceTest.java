package com.myname.finguard.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.myname.finguard.accounts.dto.CreateAccountRequest;
import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.accounts.service.AccountBalanceService;
import com.myname.finguard.accounts.service.AccountService;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.model.Role;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.MoneyConversionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private AccountBalanceService accountBalanceService;
    @Mock
    private MoneyConversionService moneyConversionService;

    private AccountService accountService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository, userRepository, currencyService, accountBalanceService, moneyConversionService);
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
        when(userRepository.findById(42L)).thenReturn(Optional.of(user(42L, "user@example.com")));
        when(moneyConversionService.sumToBaseOrNull(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(new BigDecimal("10.50"));

        UserBalanceResponse result = accountService.getUserBalance(42L);

        verify(accountRepository).findByUserId(42L);
        assertThat(result.accounts()).hasSize(3);
        assertThat(result.totalsByCurrency()).containsExactly(
                new UserBalanceResponse.CurrencyBalance("EUR", BigDecimal.ZERO),
                new UserBalanceResponse.CurrencyBalance("USD", new BigDecimal("10.50"))
        );
        assertThat(result.baseCurrency()).isEqualTo("USD");
        assertThat(result.totalInBase()).isEqualByComparingTo("10.50");
    }

    @Test
    void createAccountTrimsNameNormalizesCurrencyAndSetsBalances() {
        User user = user(42L, "user@example.com");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(currencyService.normalize("usd")).thenReturn("USD");
        when(currencyService.isSupported("USD")).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saving = invocation.getArgument(0);
            saving.setId(101L);
            return saving;
        });

        CreateAccountRequest request = new CreateAccountRequest("  Visa  ", "usd", new BigDecimal("12.34"));
        UserBalanceResponse.AccountBalance created = accountService.createAccount(42L, request);

        assertThat(created.id()).isEqualTo(101L);
        assertThat(created.name()).isEqualTo("Visa");
        assertThat(created.currency()).isEqualTo("USD");
        assertThat(created.balance()).isEqualByComparingTo("12.34");
        assertThat(created.archived()).isFalse();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getName()).isEqualTo("Visa");
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getInitialBalance()).isEqualByComparingTo("12.34");
        assertThat(saved.getCurrentBalance()).isEqualByComparingTo("12.34");
        assertThat(saved.isArchived()).isFalse();
    }

    @Test
    void createAccountDefaultsInitialBalanceToZeroWhenNull() {
        User user = user(1L, "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(currencyService.normalize("USD")).thenReturn("USD");
        when(currencyService.isSupported("USD")).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saving = invocation.getArgument(0);
            saving.setId(99L);
            return saving;
        });

        CreateAccountRequest request = new CreateAccountRequest("Cash", "USD", null);
        UserBalanceResponse.AccountBalance created = accountService.createAccount(1L, request);

        assertThat(created.balance()).isEqualByComparingTo(BigDecimal.ZERO);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getInitialBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createAccountRejectsUnsupportedCurrency() {
        User user = user(1L, "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(currencyService.normalize("ZZZ")).thenReturn("ZZZ");
        when(currencyService.isSupported("ZZZ")).thenReturn(false);

        assertThatThrownBy(() -> accountService.createAccount(1L, new CreateAccountRequest("Test", "ZZZ", BigDecimal.ZERO)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Unsupported currency");
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

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setRole(Role.USER);
        return u;
    }
}
