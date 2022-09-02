CREATE TABLE project_flag_notifications
(
    id bigserial NOT NULL
        CONSTRAINT project_flag_notifications_pkey
            PRIMARY KEY,
    flag_id         bigint NOT NULL
        CONSTRAINT project_flag_notifications_flag_id_fkey
            REFERENCES project_flags
            ON DELETE CASCADE,
    notification_id bigint NOT NULL
        CONSTRAINT project_flag_notifications_notification_id_fkey
            REFERENCES notifications
            ON DELETE CASCADE,
    user_id bigint NOT NULL
        CONSTRAINT project_flag_notifications_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT project_flag_notifications_notification_key
        UNIQUE (notification_id)
);
