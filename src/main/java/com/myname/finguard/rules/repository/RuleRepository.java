package com.myname.finguard.rules.repository;

import com.myname.finguard.rules.model.Rule;
import com.myname.finguard.rules.model.RuleStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByUserId(Long userId);

    Optional<Rule> findByIdAndUserId(Long id, Long userId);

    Page<Rule> findByStatus(RuleStatus status, Pageable pageable);
}
