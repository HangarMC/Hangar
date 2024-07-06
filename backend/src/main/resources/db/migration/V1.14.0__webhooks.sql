CREATE TABLE webhooks
(
    id         bigserial          NOT NULL
        CONSTRAINT webhooks_pkey
            PRIMARY KEY,
    name       varchar(255)       NOT NULL,
    url        varchar(255)       NOT NULL,
    secret     varchar(255),
    active     boolean            NOT NULL,
    type       varchar(255)       NOT NULL,
    events     varchar(255) array NOT NULL,
    scope      varchar(255)       NOT NULL,
    created_at timestamp          NOT NULL
);
