package com.myname.finguard.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.categories.model.Category;
import com.myname.finguard.categories.model.CategoryType;
import com.myname.finguard.categories.repository.CategoryRepository;
import com.myname.finguard.common.model.Role;
import com.myname.finguard.transactions.model.Transaction;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class RepositoryScopingIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @Transactional
    void accountRepositoryScopesByUserId() {
        User user1 = saveUser("user1@example.com");
        User user2 = saveUser("user2@example.com");

        Account account = saveAccount(user1, "Main", "USD", false);

        assertThat(accountRepository.findByIdAndUserId(account.getId(), user1.getId())).isPresent();
        assertThat(accountRepository.findByIdAndUserId(account.getId(), user2.getId())).isEmpty();
    }

    @Test
    @Transactional
    void accountRepositoryFiltersArchived() {
        User user = saveUser("arch@example.com");
        saveAccount(user, "Active", "USD", false);
        saveAccount(user, "Archived", "USD", true);

        assertThat(accountRepository.findByUserIdAndArchivedFalse(user.getId()))
                .extracting(Account::isArchived)
                .containsExactly(false);
    }

    @Test
    @Transactional
    void categoryRepositoryIncludesGlobalCategories() {
        User user = saveUser("cat@example.com");
        saveCategory(null, "Global", CategoryType.EXPENSE);
        saveCategory(user, "Mine", CategoryType.EXPENSE);

        assertThat(categoryRepository.findByUserIdOrUserIsNull(user.getId()))
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Global", "Mine");
    }

    @Test
    @Transactional
    void categoryRepositoryScopesByUserIdAndExcludesGlobal() {
        User user1 = saveUser("cat1@example.com");
        User user2 = saveUser("cat2@example.com");
        Category global = saveCategory(null, "Global", CategoryType.EXPENSE);
        Category mine = saveCategory(user1, "Mine", CategoryType.EXPENSE);

        assertThat(categoryRepository.findByIdAndUserId(mine.getId(), user1.getId())).isPresent();
        assertThat(categoryRepository.findByIdAndUserId(mine.getId(), user2.getId())).isEmpty();
        assertThat(categoryRepository.findByIdAndUserId(global.getId(), user1.getId())).isEmpty();
    }

    @Test
    @Transactional
    void transactionRepositoryScopesByUserId() {
        User user1 = saveUser("tx1@example.com");
        User user2 = saveUser("tx2@example.com");

        Account account = saveAccount(user1, "Main", "USD", false);
        Category category = saveCategory(user1, "Food", CategoryType.EXPENSE);
        Transaction tx = saveTransaction(user1, account, category);

        assertThat(transactionRepository.findByIdAndUserId(tx.getId(), user1.getId())).isPresent();
        assertThat(transactionRepository.findByIdAndUserId(tx.getId(), user2.getId())).isEmpty();
    }

    private User saveUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(Role.USER);
        user.setEmailVerified(true);
        user.setFullName("User");
        user.setBaseCurrency("USD");
        return userRepository.save(user);
    }

    private Account saveAccount(User user, String name, String currency, boolean archived) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setCurrency(currency);
        account.setInitialBalance(BigDecimal.ZERO);
        account.setCurrentBalance(BigDecimal.ZERO);
        account.setArchived(archived);
        return accountRepository.save(account);
    }

    private Category saveCategory(User user, String name, CategoryType type) {
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setType(type);
        return categoryRepository.save(category);
    }

    private Transaction saveTransaction(User user, Account account, Category category) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAccount(account);
        tx.setCategory(category);
        tx.setType(TransactionType.EXPENSE);
        tx.setAmount(new BigDecimal("10.00"));
        tx.setCurrency(account.getCurrency());
        tx.setTransactionDate(Instant.now());
        tx.setDescription("Test");
        return transactionRepository.save(tx);
    }
}

