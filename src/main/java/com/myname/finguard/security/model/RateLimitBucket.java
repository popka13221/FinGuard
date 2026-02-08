package com.myname.finguard.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rate_limit_buckets")
public class RateLimitBucket {

    @Id
    @Column(name = "bucket_key", length = 255, nullable = false)
    private String bucketKey;

    @Column(name = "window_start_ms", nullable = false)
    private long windowStartMs;

    @Column(name = "window_ms", nullable = false)
    private long windowMs;

    @Column(name = "expires_at_ms", nullable = false)
    private long expiresAtMs;

    @Column(nullable = false)
    private int count;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public String getBucketKey() {
        return bucketKey;
    }

    public void setBucketKey(String bucketKey) {
        this.bucketKey = bucketKey;
    }

    public long getWindowStartMs() {
        return windowStartMs;
    }

    public void setWindowStartMs(long windowStartMs) {
        this.windowStartMs = windowStartMs;
    }

    public long getWindowMs() {
        return windowMs;
    }

    public void setWindowMs(long windowMs) {
        this.windowMs = windowMs;
    }

    public long getExpiresAtMs() {
        return expiresAtMs;
    }

    public void setExpiresAtMs(long expiresAtMs) {
        this.expiresAtMs = expiresAtMs;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
