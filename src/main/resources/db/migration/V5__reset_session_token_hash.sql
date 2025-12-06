-- Store reset session tokens as hashes only
DELETE FROM password_reset_sessions;

ALTER TABLE password_reset_sessions DROP COLUMN jti;
ALTER TABLE password_reset_sessions ADD COLUMN token_hash VARCHAR(128) NOT NULL UNIQUE;

CREATE INDEX IF NOT EXISTS idx_password_reset_sessions_token_hash ON password_reset_sessions(token_hash);
