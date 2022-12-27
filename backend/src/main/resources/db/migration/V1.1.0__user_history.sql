create table users_history
(
    uuid     uuid                     not null
        constraint users_history_users_uuid_fk
            references users (uuid),
    old_name varchar(255)             not null,
    new_name varchar(255)             not null,
    date     timestamp with time zone not null
);
