package com.myname.finguard.transactions.repository;

import com.myname.finguard.transactions.model.Transaction;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, Instant from, Instant to);
}
