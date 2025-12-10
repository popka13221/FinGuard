package com.yourname.finguard.security.repository;

import com.yourname.finguard.security.model.OtpCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OtpCodeRepository extends JpaRepository<OtpCode, String> {

    Optional<OtpCode> findByEmail(String email);

    @Modifying
    @Query("delete from OtpCode c where c.expiresAt < ?1")
    void deleteExpired(Instant now);

    List<OtpCode> findTop50ByOrderByCreatedAtAsc();
}
