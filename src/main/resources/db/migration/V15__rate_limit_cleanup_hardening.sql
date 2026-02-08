-- Harden rate limiter cleanup path for Postgres: explicit expiry column + indexes.

ALTER TABLE rate_limit_buckets
    ADD COLUMN IF NOT EXISTS expires_at_ms BIGINT;

UPDATE rate_limit_buckets
SET expires_at_ms = window_start_ms + window_ms
WHERE expires_at_ms IS NULL;

ALTER TABLE rate_limit_buckets
    ALTER COLUMN expires_at_ms SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_rate_limit_buckets_expires_at_ms
    ON rate_limit_buckets (expires_at_ms);

CREATE INDEX IF NOT EXISTS idx_rate_limit_buckets_updated_at
    ON rate_limit_buckets (updated_at);
