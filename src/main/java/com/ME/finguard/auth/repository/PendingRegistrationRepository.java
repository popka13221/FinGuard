package com.yourname.finguard.auth.repository;

import com.yourname.finguard.auth.model.PendingRegistration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("delete from PendingRegistration p where p.verifyExpiresAt < ?1")
    void deleteExpired(Instant now);
}
