-- user_tokens.token_hash represents a short OTP-like code (6 digits in our case).
-- Such codes can collide across users, therefore a global UNIQUE constraint on token_hash is incorrect.

DROP INDEX IF EXISTS uk_user_tokens_token_hash;

CREATE INDEX IF NOT EXISTS idx_user_tokens_token_hash_type ON user_tokens(token_hash, type);
