-- Improve hot-path dashboard lookups: recurring upcoming and latest analysis job.
CREATE INDEX IF NOT EXISTS idx_wallet_insights_user_type_next_charge
    ON wallet_insights(user_id, insight_type, next_estimated_charge_at);

CREATE INDEX IF NOT EXISTS idx_crypto_wallet_analysis_jobs_wallet_user_created
    ON crypto_wallet_analysis_jobs(wallet_id, user_id, created_at);
