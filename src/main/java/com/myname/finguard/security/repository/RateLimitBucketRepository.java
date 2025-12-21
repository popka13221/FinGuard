package com.myname.finguard.security.repository;

import com.myname.finguard.security.model.RateLimitBucket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RateLimitBucketRepository extends JpaRepository<RateLimitBucket, String> {

    Optional<RateLimitBucket> findByBucketKey(String key);

    @Modifying
    @Query("delete from RateLimitBucket b where (b.windowStartMs + b.windowMs) < :nowMs")
    @Transactional
    void deleteExpired(@Param("nowMs") long nowMs);

    List<RateLimitBucket> findTop100ByOrderByUpdatedAtAsc();
}
