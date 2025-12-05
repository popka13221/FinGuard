package com.yourname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimiterServiceTest {

    @Test
    void evictsOldBucketsWhenOverCapacity() {
        RateLimiterService limiter = new RateLimiterService(5, 60000, 3);

        limiter.allow("k1");
        limiter.allow("k2");
        limiter.allow("k3");
        limiter.allow("k4"); // triggers eviction

        assertThat(limiter.getApproximateBucketCount()).isLessThanOrEqualTo(3);
    }

    @Test
    void removesExpiredBuckets() throws InterruptedException {
        RateLimiterService limiter = new RateLimiterService(1, 10, 100);
        limiter.allow("short");
        Thread.sleep(15);
        limiter.allow("other");
        assertThat(limiter.getApproximateBucketCount()).isEqualTo(1);
    }
}
