package com.myname.finguard.security.repository;

import com.myname.finguard.security.model.LoginAttempt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {
    Optional<LoginAttempt> findByEmail(String email);
}
