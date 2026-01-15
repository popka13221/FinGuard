package com.myname.finguard.transactions.repository;

import com.myname.finguard.transactions.model.Transaction;
import com.myname.finguard.transactions.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, Instant from, Instant to);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(Long userId, Instant from, Instant to);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    long countByAccountIdAndUserId(Long accountId, Long userId);

    long countByCategoryIdAndUserId(Long categoryId, Long userId);

    @Query("""
            select t.type as type, t.currency as currency, coalesce(sum(t.amount), 0) as total
            from Transaction t
            where t.user.id = :userId and t.transactionDate between :from and :to
            group by t.type, t.currency
            """)
    List<TypeCurrencyTotal> sumByTypeAndCurrency(@Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select
                t.category.id as categoryId,
                t.category.name as categoryName,
                t.type as type,
                t.currency as currency,
                coalesce(sum(t.amount), 0) as total
            from Transaction t
            where t.user.id = :userId and t.transactionDate between :from and :to
            group by t.category.id, t.category.name, t.type, t.currency
            """)
    List<CategoryTypeCurrencyTotal> sumByCategoryTypeAndCurrency(
            @Param("userId") Long userId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select t.transactionDate as transactionDate, t.type as type, t.currency as currency, t.amount as amount
            from Transaction t
            where t.user.id = :userId and t.transactionDate between :from and :to
            """)
    List<CashFlowRow> findCashFlowRows(@Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select t.currency as currency, coalesce(sum(t.amount), 0) as total
            from Transaction t
            where t.user.id = :userId
              and t.category.id = :categoryId
              and t.type = :type
              and t.transactionDate between :from and :to
            group by t.currency
            """)
    List<CategoryCurrencyTotal> sumByCategoryAndTypeAndCurrency(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select coalesce(sum(
                case
                    when t.type = com.myname.finguard.transactions.model.TransactionType.INCOME then t.amount
                    else -t.amount
                end
            ), 0)
            from Transaction t
            where t.user.id = :userId and t.account.id = :accountId
            """)
    BigDecimal sumNetByUserIdAndAccountId(@Param("userId") Long userId, @Param("accountId") Long accountId);

    interface TypeCurrencyTotal {
        TransactionType getType();

        String getCurrency();

        BigDecimal getTotal();
    }

    interface CategoryTypeCurrencyTotal {
        Long getCategoryId();

        String getCategoryName();

        TransactionType getType();

        String getCurrency();

        BigDecimal getTotal();
    }

    interface CashFlowRow {
        Instant getTransactionDate();

        TransactionType getType();

        String getCurrency();

        BigDecimal getAmount();
    }

    interface CategoryCurrencyTotal {
        String getCurrency();

        BigDecimal getTotal();
    }
}
