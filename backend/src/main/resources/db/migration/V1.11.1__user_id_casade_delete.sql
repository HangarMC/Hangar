ALTER TABLE user_credentials DROP CONSTRAINT user_credentials_users_id_fk;
ALTER TABLE user_credentials
    ADD CONSTRAINT user_credentials_users_id_fk
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE verification_codes DROP CONSTRAINT verification_codes_users_id_fk;
ALTER TABLE verification_codes
    ADD CONSTRAINT verification_codes_users_id_fk
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;
