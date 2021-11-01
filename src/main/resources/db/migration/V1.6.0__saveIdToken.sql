CREATE TABLE user_oauth_token(
    id bigserial NOT NULL
        CONSTRAINT user_oauth_token_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    username varchar(255),
    id_token text
)
