package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.security.model.RateLimitBucket;
import com.myname.finguard.security.repository.RateLimitBucketRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.CannotAcquireLockException;

class RateLimiterServiceTest {

    @Test
    void evictsOldBucketsWhenOverCapacity() {
        AtomicLong now = new AtomicLong(1_000);
        RateLimiterService limiter = new RateLimiterService(5, 60000, 3, now::get);

        limiter.allow("k1");
        limiter.allow("k2");
        limiter.allow("k3");
        limiter.allow("k4"); // triggers eviction

        assertThat(limiter.getApproximateBucketCount()).isLessThanOrEqualTo(3);
    }

    @Test
    void blocksWhenLimitExceededAndAllowsAfterWindowReset() {
        AtomicLong now = new AtomicLong(1_000);
        RateLimiterService limiter = new RateLimiterService(2, 20, 100, now::get);

        assertThat(limiter.allow("key")).isTrue();
        assertThat(limiter.allow("key")).isTrue();
        assertThat(limiter.allow("key")).isFalse();

        now.addAndGet(25);

        assertThat(limiter.allow("key")).isTrue();
    }

    @Test
    void removesExpiredBuckets() {
        AtomicLong now = new AtomicLong(1_000);
        RateLimiterService limiter = new RateLimiterService(1, 10, 100, now::get);
        limiter.allow("short");
        now.addAndGet(15);
        limiter.allow("other");
        assertThat(limiter.getApproximateBucketCount()).isEqualTo(1);
    }

    @Test
    void hashesBucketKeyBeforePersistingToRepository() {
        RateLimitBucketRepository repo = mock(RateLimitBucketRepository.class);
        when(repo.findByBucketKey(anyString())).thenReturn(Optional.empty());
        when(repo.save(any(RateLimitBucket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.count()).thenReturn(0L);

        AtomicLong now = new AtomicLong(1_000);
        RateLimiterService limiter = new RateLimiterService(repo, 5, 60_000, 1000, now::get);
        limiter.check("login:email:user@example.com", 5, 60_000);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(repo).findByBucketKey(keyCaptor.capture());
        assertThat(keyCaptor.getValue()).startsWith("rl:");
        assertThat(keyCaptor.getValue()).doesNotContain("user@example.com");

        ArgumentCaptor<RateLimitBucket> bucketCaptor = ArgumentCaptor.forClass(RateLimitBucket.class);
        verify(repo).save(bucketCaptor.capture());
        assertThat(bucketCaptor.getValue().getBucketKey()).startsWith("rl:");
        assertThat(bucketCaptor.getValue().getBucketKey()).doesNotContain("user@example.com");
    }

    @Test
    void allowsRequestWhenRepositoryIsUnderLockContention() {
        RateLimitBucketRepository repo = mock(RateLimitBucketRepository.class);
        when(repo.findByBucketKey(anyString())).thenThrow(new CannotAcquireLockException("deadlock"));

        AtomicLong now = new AtomicLong(1_000);
        RateLimiterService limiter = new RateLimiterService(repo, 5, 60_000, 1000, now::get);

        RateLimiterService.Result result = limiter.check("public-rates:203.0.113.15", 5, 60_000);

        assertThat(result.allowed()).isTrue();
        assertThat(result.retryAfterMs()).isZero();
    }
}
