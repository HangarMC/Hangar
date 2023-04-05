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

CREATE TABLE avatars
(
    id               bigserial                NOT NULL
        CONSTRAINT avatars_pk
            PRIMARY KEY,
    subject          varchar(255)             NOT NULL,
    type             varchar(16)              NOT NULL,
    optimized_hash   varchar(32)              NOT NULL,
    unoptimized_hash varchar(32)              NOT NULL,
    created_at       timestamp WITH TIME ZONE NOT NULL,
    version          int                      NOT NULL,
    CONSTRAINT avatars_unique
        UNIQUE (subject, type)
);

CREATE TABLE verification_codes
(
    id         bigserial
        CONSTRAINT verification_codes_pk
            PRIMARY KEY,
    user_id    integer                  NOT NULL
        CONSTRAINT verification_codes_users_id_fk
            REFERENCES users,
    created_at timestamp WITH TIME ZONE NOT NULL,
    type       integer                  NOT NULL,
    code       varchar(16)              NOT NULL
);

DROP TABLE user_oauth_token;

DROP TABLE user_sign_ons;

ALTER TABLE users
    ADD email_verified bool DEFAULT FALSE NOT NULL;
ALTER TABLE users
    ADD socials jsonb DEFAULT '{}' NOT NULL;
