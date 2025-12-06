-- Hash user_tokens.token and drop plaintext storage; add per-user token version.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Add token_version to users to support token invalidation by version.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;

-- Add hashed token column and backfill from existing plaintext token.
ALTER TABLE user_tokens
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(128);

UPDATE user_tokens
SET token_hash = encode(digest(token, 'sha256'), 'hex')
WHERE token_hash IS NULL;

ALTER TABLE user_tokens
    ALTER COLUMN token_hash SET NOT NULL;

-- Replace uniqueness on plaintext token with hash-only uniqueness.
ALTER TABLE user_tokens DROP CONSTRAINT IF EXISTS user_tokens_token_key;
ALTER TABLE user_tokens
    ADD CONSTRAINT uq_user_tokens_token_hash UNIQUE (token_hash);

-- Drop plaintext token column.
ALTER TABLE user_tokens DROP COLUMN IF EXISTS token;
