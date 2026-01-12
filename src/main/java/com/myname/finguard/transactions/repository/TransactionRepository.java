package com.myname.finguard.transactions.repository;

import com.myname.finguard.transactions.model.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, Instant from, Instant to);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(Long userId, Instant from, Instant to);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    long countByAccountIdAndUserId(Long accountId, Long userId);

    long countByCategoryIdAndUserId(Long categoryId, Long userId);

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
}
