CREATE TABLE announcements
(
    id         bigserial PRIMARY KEY,
    text       text                     NOT NULL,
    color      varchar(20)              NOT NULL DEFAULT 'blue',
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    created_by bigint                   NOT NULL REFERENCES users (id)
);

CREATE TABLE global_notifications
(
    id          bigserial PRIMARY KEY,
    key         varchar(100) UNIQUE      NOT NULL,
    content     text                     NOT NULL,
    color       varchar(20)              NOT NULL DEFAULT 'blue',
    active_from timestamp with time zone NOT NULL DEFAULT now(),
    active_to   timestamp with time zone NOT NULL DEFAULT '9999-12-31 23:59:59+00'::timestamp with time zone,
    created_at  timestamp with time zone NOT NULL DEFAULT now(),
    created_by  bigint                   NOT NULL REFERENCES users (id)
)
