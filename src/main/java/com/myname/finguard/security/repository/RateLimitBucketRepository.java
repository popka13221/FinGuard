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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            with expired as (
                select ctid
                from rate_limit_buckets
                where expires_at_ms < :nowMs
                order by expires_at_ms asc
                for update skip locked
                limit :batchSize
            )
            delete from rate_limit_buckets b
            using expired
            where b.ctid = expired.ctid
            """, nativeQuery = true)
    @Transactional
    int deleteExpiredBatchSkipLocked(@Param("nowMs") long nowMs, @Param("batchSize") int batchSize);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete from rate_limit_buckets
            where bucket_key in (
                select bucket_key
                from rate_limit_buckets
                where expires_at_ms < :nowMs
                order by expires_at_ms asc
                limit :batchSize
            )
            """, nativeQuery = true)
    @Transactional
    int deleteExpiredBatchPortable(@Param("nowMs") long nowMs, @Param("batchSize") int batchSize);

    List<RateLimitBucket> findTop100ByOrderByUpdatedAtAsc();
}
