-- Align DB with code expectations:
-- - users.token_version for token invalidation on password reset
-- - user_tokens.token_hash (hashed tokens in code)
-- Note: existing tokens are copied into token_hash as-is; they will not validate with hashed lookups
--       and should be re-issued after migration.

-- Add per-user token version (used to invalidate auth tokens on password change)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS token_version INT NOT NULL DEFAULT 0;

-- Add hashed token column
ALTER TABLE user_tokens
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(128);

-- Populate token_hash from legacy token values so the column can be made NOT NULL.
UPDATE user_tokens
SET token_hash = token
WHERE token_hash IS NULL;

-- Enforce not-null on the new column
ALTER TABLE user_tokens
    ALTER COLUMN token_hash SET NOT NULL;

-- Drop legacy plaintext token column
ALTER TABLE user_tokens
    DROP COLUMN IF EXISTS token;

-- Ensure uniqueness by hash (new tokens are issued hashed in code)
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_tokens_token_hash
    ON user_tokens(token_hash);
