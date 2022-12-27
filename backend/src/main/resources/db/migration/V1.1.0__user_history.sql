CREATE TABLE users_history
(
    uuid     uuid                     NOT NULL
        CONSTRAINT users_history_users_uuid_fk
            REFERENCES users (uuid),
    old_name varchar(255)             NOT NULL,
    new_name varchar(255)             NOT NULL,
    date     timestamp WITH TIME ZONE NOT NULL
);
