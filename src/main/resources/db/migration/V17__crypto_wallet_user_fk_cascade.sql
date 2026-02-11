ALTER TABLE crypto_wallets
    DROP CONSTRAINT fk_crypto_wallets_user;

ALTER TABLE crypto_wallets
    ADD CONSTRAINT fk_crypto_wallets_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
