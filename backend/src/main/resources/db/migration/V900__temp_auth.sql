CREATE TABLE user_credentials
(
    id         bigserial
        CONSTRAINT user_credentials_pk
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    updated_at timestamp WITH TIME ZONE,
    user_id    integer                  NOT NULL
        CONSTRAINT user_credentials_users_id_fk
            REFERENCES users,
    credential jsonb                    NOT NULL,
    type       integer,
    CONSTRAINT user_credentials_unique
        UNIQUE (type, user_id)
);


