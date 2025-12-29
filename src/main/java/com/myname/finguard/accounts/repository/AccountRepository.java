package com.myname.finguard.accounts.repository;

import com.myname.finguard.accounts.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    List<Account> findByUserIdAndArchivedFalse(Long userId);
}
