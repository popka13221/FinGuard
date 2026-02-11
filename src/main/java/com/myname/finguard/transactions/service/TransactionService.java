package com.myname.finguard.transactions.service;

import com.myname.finguard.accounts.model.Account;
import com.myname.finguard.accounts.repository.AccountRepository;
import com.myname.finguard.accounts.service.AccountBalanceService;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.categories.model.Category;
import com.myname.finguard.categories.service.CategoryService;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.transactions.dto.CreateTransactionRequest;
import com.myname.finguard.transactions.dto.TransactionDto;
import com.myname.finguard.transactions.dto.UpdateTransactionRequest;
import com.myname.finguard.transactions.model.Transaction;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
import com.myname.finguard.dashboard.events.UserDataChangedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class TransactionService {

    private static final int MAX_LIST_LIMIT = 200;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final AccountBalanceService accountBalanceService;
    @Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository,
            CategoryService categoryService,
            AccountBalanceService accountBalanceService
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
            this.accountBalanceService = accountBalanceService;
    }

    public List<TransactionDto> listTransactions(Long userId, Instant from, Instant to, Integer limit) {
        if (userId == null) {
            throw unauthorized();
        }
        Instant now = Instant.now();
        Instant start = from == null ? now.minus(30, ChronoUnit.DAYS) : from;
        Instant end = to == null ? now.plus(1, ChronoUnit.DAYS) : to;
        if (end.isBefore(start)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "`to` must be after `from`", HttpStatus.BAD_REQUEST);
        }

        List<Transaction> rows;
        if (limit != null) {
            int bounded = Math.min(Math.max(limit, 1), MAX_LIST_LIMIT);
            if (limit != bounded) {
                throw new ApiException(ErrorCodes.BAD_REQUEST, "`limit` must be between 1 and " + MAX_LIST_LIMIT, HttpStatus.BAD_REQUEST);
            }
            rows = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    userId,
                    start,
                    end,
                    PageRequest.of(0, bounded)
            );
        } else {
            rows = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end);
        }

        return rows.stream()
                .map(this::toDto)
                .toList();
    }

    public TransactionDto getTransaction(Long userId, Long transactionId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (transactionId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Transaction id is required", HttpStatus.BAD_REQUEST);
        }
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Transaction not found", HttpStatus.BAD_REQUEST));
        return toDto(tx);
    }

    public TransactionDto createTransaction(Long userId, CreateTransactionRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        Account account = requireActiveAccount(userId, request.accountId());
        Category category = categoryService.requireAccessibleCategory(userId, request.categoryId());
        TransactionType type = requireType(request.type());
        if (!categoryService.isCategoryCompatible(category, type)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category type is not compatible with transaction type", HttpStatus.BAD_REQUEST);
        }
        BigDecimal amount = requirePositiveAmount(request.amount());
        Instant date = requireDate(request.transactionDate());
        String description = normalizeDescription(request.description());

        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAccount(account);
        tx.setCategory(category);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setCurrency(account.getCurrency());
        tx.setTransactionDate(date);
        tx.setDescription(description);

        Transaction saved = transactionRepository.save(tx);
        recalcAccount(userId, account.getId());
        publishUserDataChanged(userId);
        return toDto(saved);
    }

    public TransactionDto updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (transactionId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Transaction id is required", HttpStatus.BAD_REQUEST);
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Transaction not found", HttpStatus.BAD_REQUEST));

        Long oldAccountId = tx.getAccount() == null ? null : tx.getAccount().getId();
        Long nextAccountId = request.accountId() != null ? request.accountId() : oldAccountId;
        Account account = requireActiveAccount(userId, nextAccountId);

        Long oldCategoryId = tx.getCategory() == null ? null : tx.getCategory().getId();
        Long nextCategoryId = request.categoryId() != null ? request.categoryId() : oldCategoryId;
        Category category = categoryService.requireAccessibleCategory(userId, nextCategoryId);

        TransactionType type = request.type() != null ? request.type() : tx.getType();
        type = requireType(type);
        if (!categoryService.isCategoryCompatible(category, type)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category type is not compatible with transaction type", HttpStatus.BAD_REQUEST);
        }

        BigDecimal amount = request.amount() != null ? request.amount() : tx.getAmount();
        amount = requirePositiveAmount(amount);

        Instant date = request.transactionDate() != null ? request.transactionDate() : tx.getTransactionDate();
        date = requireDate(date);

        if (request.description() != null) {
            tx.setDescription(normalizeDescription(request.description()));
        }

        tx.setAccount(account);
        tx.setCategory(category);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setCurrency(account.getCurrency());
        tx.setTransactionDate(date);

        Transaction saved = transactionRepository.save(tx);

        recalcAccount(userId, nextAccountId);
        if (oldAccountId != null && !Objects.equals(oldAccountId, nextAccountId)) {
            recalcAccount(userId, oldAccountId);
        }
        publishUserDataChanged(userId);

        return toDto(saved);
    }

    public void deleteTransaction(Long userId, Long transactionId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (transactionId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Transaction id is required", HttpStatus.BAD_REQUEST);
        }
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Transaction not found", HttpStatus.BAD_REQUEST));
        Long accountId = tx.getAccount() == null ? null : tx.getAccount().getId();
        transactionRepository.delete(tx);
        recalcAccount(userId, accountId);
        publishUserDataChanged(userId);
    }

    private Account requireActiveAccount(Long userId, Long accountId) {
        if (accountId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Account id is required", HttpStatus.BAD_REQUEST);
        }
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Account not found", HttpStatus.BAD_REQUEST));
        if (account.isArchived()) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Account is archived", HttpStatus.BAD_REQUEST);
        }
        return account;
    }

    private TransactionType requireType(TransactionType type) {
        if (type == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Transaction type is required", HttpStatus.BAD_REQUEST);
        }
        return type;
    }

    private BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Amount is required", HttpStatus.BAD_REQUEST);
        }
        if (amount.signum() <= 0) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Amount must be positive", HttpStatus.BAD_REQUEST);
        }
        return amount;
    }

    private Instant requireDate(Instant date) {
        if (date == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Transaction date is required", HttpStatus.BAD_REQUEST);
        }
        return date;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private void recalcAccount(Long userId, Long accountId) {
        if (accountBalanceService == null || accountId == null) {
            return;
        }
        accountBalanceService.recalculateAndPersist(userId, accountId);
    }

    private TransactionDto toDto(Transaction tx) {
        Long accountId = tx.getAccount() == null ? null : tx.getAccount().getId();
        Long categoryId = tx.getCategory() == null ? null : tx.getCategory().getId();
        return new TransactionDto(
                tx.getId(),
                accountId,
                categoryId,
                tx.getType(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getTransactionDate(),
                tx.getDescription()
        );
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
