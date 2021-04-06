ALTER TABLE api_keys ADD CONSTRAINT api_keys_owner_id_token_identifier UNIQUE (owner_id, token_identifier);

DROP TABLE api_sessions;
