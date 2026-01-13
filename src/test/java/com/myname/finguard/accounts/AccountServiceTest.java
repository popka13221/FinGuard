package com.myname.finguard.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.myname.finguard.accounts.dto.AccountDto;
import com.myname.finguard.accounts.dto.CreateAccountRequest;
import com.myname.finguard.accounts.dto.UpdateAccountRequest;
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

    @Test
    void listAccountsMapsNullBalancesToZero() {
        Account a1 = new Account();
        a1.setId(1L);
        a1.setName("Cash");
        a1.setCurrency("USD");
        a1.setInitialBalance(null);
        a1.setCurrentBalance(null);
        a1.setArchived(false);

        Account a2 = new Account();
        a2.setId(2L);
        a2.setName("Card");
        a2.setCurrency("EUR");
        a2.setInitialBalance(new BigDecimal("10.00"));
        a2.setCurrentBalance(new BigDecimal("7.50"));
        a2.setArchived(true);

        when(accountRepository.findByUserId(42L)).thenReturn(List.of(a1, a2));

        List<AccountDto> result = accountService.listAccounts(42L);

        assertThat(result).containsExactly(
                new AccountDto(1L, "Cash", "USD", BigDecimal.ZERO, BigDecimal.ZERO, false),
                new AccountDto(2L, "Card", "EUR", new BigDecimal("10.00"), new BigDecimal("7.50"), true)
        );
    }

    @Test
    void updateAccountRecalculatesBalanceWhenInitialBalanceChanges() {
        Account existing = new Account();
        existing.setId(7L);
        existing.setName("Old");
        existing.setCurrency("USD");
        existing.setInitialBalance(new BigDecimal("10.00"));
        existing.setCurrentBalance(new BigDecimal("12.00"));
        existing.setArchived(false);

        when(accountRepository.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(existing));

        Account recalculated = new Account();
        recalculated.setId(7L);
        recalculated.setName("New Name");
        recalculated.setCurrency("USD");
        recalculated.setInitialBalance(new BigDecimal("20.00"));
        recalculated.setCurrentBalance(new BigDecimal("99.99"));
        recalculated.setArchived(true);
        when(accountBalanceService.recalculateAndPersist(42L, 7L)).thenReturn(recalculated);

        AccountDto dto = accountService.updateAccount(42L, 7L, new UpdateAccountRequest("  New Name  ", new BigDecimal("20.00"), true));

        assertThat(dto).isEqualTo(new AccountDto(7L, "New Name", "USD", new BigDecimal("20.00"), new BigDecimal("99.99"), true));
        verify(accountBalanceService).recalculateAndPersist(42L, 7L);

        ArgumentCaptor<Account> savedCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(savedCaptor.capture());
        Account saved = savedCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("New Name");
        assertThat(saved.getInitialBalance()).isEqualByComparingTo("20.00");
        assertThat(saved.isArchived()).isTrue();
    }

    @Test
    void updateAccountDoesNotRecalculateWhenInitialBalanceUnchanged() {
        Account existing = new Account();
        existing.setId(7L);
        existing.setName("Old");
        existing.setCurrency("USD");
        existing.setInitialBalance(new BigDecimal("10.00"));
        existing.setCurrentBalance(new BigDecimal("12.00"));
        existing.setArchived(false);

        when(accountRepository.findByIdAndUserId(7L, 42L)).thenReturn(Optional.of(existing));

        AccountDto dto = accountService.updateAccount(42L, 7L, new UpdateAccountRequest("New", new BigDecimal("10.0"), null));

        assertThat(dto.name()).isEqualTo("New");
        assertThat(dto.initialBalance()).isEqualByComparingTo("10.00");
        assertThat(dto.currentBalance()).isEqualByComparingTo("12.00");
        verify(accountBalanceService, never()).recalculateAndPersist(any(), any());
    }

    @Test
    void updateAccountValidatesArgumentsAndNotFound() {
        assertThatThrownBy(() -> accountService.updateAccount(42L, null, new UpdateAccountRequest("x", null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Account id is required");

        assertThatThrownBy(() -> accountService.updateAccount(42L, 1L, null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Request body is required");

        when(accountRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.updateAccount(42L, 1L, new UpdateAccountRequest("x", null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Account not found");

        Account existing = new Account();
        existing.setId(1L);
        existing.setName("Old");
        existing.setCurrency("USD");
        existing.setInitialBalance(BigDecimal.ZERO);
        existing.setCurrentBalance(BigDecimal.ZERO);
        existing.setArchived(false);
        when(accountRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> accountService.updateAccount(42L, 1L, new UpdateAccountRequest("   ", null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Account name is required");
    }

    @Test
    void deleteAccountDeletesWhenNoTransactions() {
        Account existing = new Account();
        existing.setId(3L);
        existing.setName("Cash");
        existing.setCurrency("USD");
        existing.setArchived(false);
        when(accountRepository.findByIdAndUserId(3L, 42L)).thenReturn(Optional.of(existing));
        when(accountBalanceService.countTransactions(42L, 3L)).thenReturn(0L);

        accountService.deleteAccount(42L, 3L);

        verify(accountRepository).delete(existing);
    }

    @Test
    void deleteAccountRejectsWhenHasTransactions() {
        Account existing = new Account();
        existing.setId(3L);
        existing.setName("Cash");
        existing.setCurrency("USD");
        existing.setArchived(false);
        when(accountRepository.findByIdAndUserId(3L, 42L)).thenReturn(Optional.of(existing));
        when(accountBalanceService.countTransactions(42L, 3L)).thenReturn(1L);

        assertThatThrownBy(() -> accountService.deleteAccount(42L, 3L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot be deleted");

        verify(accountRepository, never()).delete(any());
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
