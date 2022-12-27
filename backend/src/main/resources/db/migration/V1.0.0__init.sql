CREATE TYPE role_category AS enum ('global', 'project', 'organization');

CREATE TYPE logged_action_type AS enum (
    'project_visibility_changed',
    'project_renamed',
    'project_flagged',
    'project_settings_changed',
    'project_icon_changed',
    'project_flag_resolved',
    'project_channel_created',
    'project_channel_edited',
    'project_channel_deleted',
    'project_invites_sent',
    'project_invite_declined',
    'project_invite_unaccepted',
    'project_member_added',
    'project_members_removed',
    'project_member_roles_changed',
    'project_page_created',
    'project_page_deleted',
    'project_page_edited',
    'version_visibility_changed',
    'version_deleted',
    'version_created',
    'version_description_changed',
    'version_review_state_changed',
    'version_plugin_dependencies_added',
    'version_plugin_dependencies_edited',
    'version_plugin_dependencies_removed',
    'version_platform_dependencies_added',
    'version_platform_dependencies_removed',
    'user_tagline_changed',
    'user_locked',
    'user_unlocked',
    'user_apikey_created',
    'user_apikey_deleted',
    'organization_invites_sent',
    'organization_invite_declined',
    'organization_invite_unaccepted',
    'organization_member_added',
    'organization_members_removed',
    'organization_member_roles_changed'
    );

CREATE TYPE job_state AS enum ('not_started', 'started', 'done', 'fatal_failure');

CREATE TABLE users
(
    id           bigserial                         NOT NULL
        CONSTRAINT users_pkey
            PRIMARY KEY,
    uuid         uuid                              NOT NULL
        CONSTRAINT users_uuid_key
            UNIQUE,
    created_at   timestamp WITH TIME ZONE          NOT NULL,
    full_name    varchar(255),
    name         varchar(255)                      NOT NULL
        CONSTRAINT users_name_key
            UNIQUE,
    email        varchar(255)
        CONSTRAINT users_email_key
            UNIQUE,
    tagline      varchar(255),
    join_date    timestamp WITH TIME ZONE,
    read_prompts integer[] DEFAULT '{}'::integer[] NOT NULL,
    locked       boolean   DEFAULT FALSE           NOT NULL,
    language     varchar(16),
    theme        varchar(16)
);

CREATE TABLE projects
(
    id               bigserial                        NOT NULL
        CONSTRAINT projects_pkey
            PRIMARY KEY,
    created_at       timestamp WITH TIME ZONE         NOT NULL,
    name             varchar(255)                     NOT NULL,
    slug             varchar(255)                     NOT NULL,
    CONSTRAINT projects_namespace_unique UNIQUE (owner_name, slug),
    owner_name       varchar(255)                     NOT NULL
        CONSTRAINT projects_owner_name_fkey
            REFERENCES users (name)
            ON UPDATE CASCADE,
    owner_id         bigint                           NOT NULL
        CONSTRAINT projects_owner_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    topic_id         integer,
    post_id          integer,
    category         integer                          NOT NULL,
    description      varchar(255),
    visibility       integer DEFAULT 1                NOT NULL,
    keywords         text[]  DEFAULT ARRAY []::text[] NOT NULL,
    homepage         varchar(255),
    issues           varchar(255),
    source           varchar(255),
    support          varchar(255),
    wiki             varchar(255),
    license_type     varchar(255),
    license_name     varchar(255),
    license_url      varchar(255),
    forum_sync       boolean DEFAULT TRUE             NOT NULL,
    donation_enabled boolean DEFAULT FALSE,
    donation_subject varchar(255),
    sponsors         text    DEFAULT ''::text         NOT NULL,
    CONSTRAINT projects_owner_name_name_key
        UNIQUE (owner_name, name),
    CONSTRAINT projects_owner_name_slug_key
        UNIQUE (owner_name, slug)
);

CREATE INDEX projects_owner_id
    ON projects (owner_id);

CREATE TABLE project_notes
(
    id         bigserial                NOT NULL
        CONSTRAINT notes_pkey PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT notes_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    message    text                     NOT NULL,
    user_id    bigint
        CONSTRAINT notes_user_id
            REFERENCES users
            ON DELETE SET NULL
);

CREATE TABLE project_stars
(
    user_id    bigint NOT NULL
        CONSTRAINT project_stars_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    project_id bigint NOT NULL
        CONSTRAINT project_stars_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    CONSTRAINT project_stars_pkey
        PRIMARY KEY (user_id, project_id)
);

CREATE TABLE project_pages
(
    id         bigserial                NOT NULL
        CONSTRAINT pages_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT pages_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    name       varchar(255)             NOT NULL,
    slug       varchar(255)             NOT NULL,
    contents   text    DEFAULT ''::text NOT NULL,
    deletable  boolean DEFAULT TRUE     NOT NULL,
    parent_id  bigint
        CONSTRAINT project_pages_parent_id_fkey
            REFERENCES project_pages
            ON DELETE RESTRICT
);

CREATE INDEX page_slug_idx
    ON project_pages (lower(slug));

CREATE INDEX page_parent_id_idx
    ON project_pages (parent_id);

CREATE TABLE project_home_pages
(
    id         bigserial                NOT NULL
        CONSTRAINT home_pages_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT home_pages_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    page_id    bigint                   NOT NULL
        CONSTRAINT home_pages_page_id_fkey
            REFERENCES project_pages
            ON DELETE RESTRICT,
    CONSTRAINT home_pages_project_id_unique
        UNIQUE (project_id)
);

CREATE INDEX page_project_id_idx
    ON project_home_pages (project_id);

CREATE TABLE project_channels
(
    id         bigserial                NOT NULL
        CONSTRAINT channels_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    name       varchar(255)             NOT NULL,
    color      integer                  NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT channels_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    flags      int[]                    NOT NULL DEFAULT '{}',
    CONSTRAINT channels_project_id_name_key
        UNIQUE (project_id, name),
    CONSTRAINT channels_project_id_color_id_key
        UNIQUE (project_id, color)
);

CREATE TABLE project_versions
(
    id                bigserial                NOT NULL
        CONSTRAINT versions_pkey
            PRIMARY KEY,
    created_at        timestamp WITH TIME ZONE NOT NULL,
    version_string    varchar(255)             NOT NULL,
    description       text,
    project_id        bigint                   NOT NULL
        CONSTRAINT versions_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    channel_id        bigint                   NOT NULL
        CONSTRAINT versions_channel_id_fkey
            REFERENCES project_channels
            ON DELETE CASCADE,
    reviewer_id       bigint
        CONSTRAINT project_versions_reviewer_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    approved_at       timestamp WITH TIME ZONE,
    author_id         bigint
        CONSTRAINT project_versions_author_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    visibility        integer DEFAULT 1        NOT NULL,
    review_state      integer DEFAULT 0        NOT NULL,
    create_forum_post boolean                  NOT NULL,
    post_id           integer,
    CONSTRAINT version_string_unique
        UNIQUE (project_id, version_string)
);

CREATE INDEX project_version_version_string_idx
    ON project_versions (version_string);

CREATE TABLE platform_versions
(
    id         bigserial                NOT NULL
        CONSTRAINT platform_versions_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    platform   bigint                   NOT NULL,
    version    varchar(255)             NOT NULL,
    CONSTRAINT platform_version_unique UNIQUE (platform, version)
);

CREATE TABLE project_version_dependencies
(
    id           bigserial                NOT NULL CONSTRAINT version_dependencies_pkey PRIMARY KEY,
    created_at   timestamp WITH TIME ZONE NOT NULL,
    version_id   bigint                   NOT NULL,
    platform     bigint                   NOT NULL,
    name         text                     NOT NULL,
    required     boolean                  NOT NULL,
    project_id   bigint,
    external_url text,
    CONSTRAINT project_version_dependencies_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_dependencies_project_id_fkey
        FOREIGN KEY (project_id)
            REFERENCES projects
            ON DELETE SET NULL,
    CONSTRAINT project_version_dependencies_unique
        UNIQUE (version_id, name, platform)
);

CREATE TABLE project_version_platform_dependencies
(
    id                  bigserial                NOT NULL CONSTRAINT version_platform_dependencies_pkey PRIMARY KEY,
    created_at          timestamp WITH TIME ZONE NOT NULL,
    version_id          bigint                   NOT NULL,
    platform_version_id bigint,
    CONSTRAINT project_version_platform_dependencies_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_dependencies_platform_version_id_fkey
        FOREIGN KEY (platform_version_id)
            REFERENCES platform_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_dependencies_unique
        UNIQUE (version_id, platform_version_id)
);

CREATE TABLE project_version_downloads
(
    id           bigserial NOT NULL PRIMARY KEY,
    version_id   bigint    NOT NULL,
    file_size    bigint DEFAULT 1
        CONSTRAINT versions_file_size_check
            CHECK (file_size > 0),
    hash         varchar(32),
    file_name    varchar(255),
    external_url varchar(255),
    CONSTRAINT project_version_downloads_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE
);

CREATE TABLE project_version_platform_downloads
(
    id          bigserial NOT NULL PRIMARY KEY,
    version_id  bigint    NOT NULL,
    platform    bigint    NOT NULL,
    download_id bigint    NOT NULL,
    CONSTRAINT project_version_platform_downloads_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_downloads_download_id_fkey
        FOREIGN KEY (download_id)
            REFERENCES project_version_downloads
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_downloads_unique
        UNIQUE (version_id, platform)
);

CREATE TABLE roles
(
    id         bigint                       NOT NULL
        CONSTRAINT roles_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE     NOT NULL,
    name       varchar(255)                 NOT NULL,
    category   role_category                NOT NULL,
    title      varchar(255)                 NOT NULL,
    color      varchar(255)                 NOT NULL,
    assignable boolean                      NOT NULL,
    rank       integer,
    permission bit(64) DEFAULT '0'::bit(64) NOT NULL
);

CREATE UNIQUE INDEX role_name_idx
    ON roles (name);

CREATE TABLE user_project_roles
(
    id         bigserial                NOT NULL
        CONSTRAINT user_project_roles_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    user_id    bigint                   NOT NULL
        CONSTRAINT user_project_roles_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    role_type  varchar                  NOT NULL
        CONSTRAINT user_project_roles_role_type_fkey
            REFERENCES roles (name)
            ON DELETE CASCADE,
    project_id bigint                   NOT NULL
        CONSTRAINT user_project_roles_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    accepted   boolean DEFAULT FALSE    NOT NULL,
    CONSTRAINT project_roles_user_id_project_id UNIQUE (user_id, project_id)
);

CREATE TABLE project_flags
(
    id          bigserial                NOT NULL
        CONSTRAINT flags_pkey
            PRIMARY KEY,
    created_at  timestamp WITH TIME ZONE NOT NULL,
    project_id  bigint                   NOT NULL
        CONSTRAINT flags_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    user_id     bigint                   NOT NULL
        CONSTRAINT flags_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    reason      integer                  NOT NULL,
    resolved    boolean DEFAULT FALSE    NOT NULL,
    comment     text                     NOT NULL,
    resolved_at timestamp WITH TIME ZONE,
    resolved_by bigint
        CONSTRAINT project_flags_resolved_by_fkey
            REFERENCES users
            ON DELETE SET NULL
);

CREATE TABLE notifications
(
    id           bigserial                NOT NULL
        CONSTRAINT notifications_pkey
            PRIMARY KEY,
    created_at   timestamp WITH TIME ZONE NOT NULL,
    user_id      bigint                   NOT NULL
        CONSTRAINT notifications_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    type         integer                  NOT NULL,
    action       varchar(255),
    read         boolean DEFAULT FALSE    NOT NULL,
    origin_id    bigint
        CONSTRAINT notifications_origin_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    message_args varchar(255)[]           NOT NULL
);

CREATE TABLE project_flag_notifications
(
    id              bigserial NOT NULL
        CONSTRAINT project_flag_notifications_pkey
            PRIMARY KEY,
    flag_id         bigint    NOT NULL
        CONSTRAINT project_flag_notifications_flag_id_fkey
            REFERENCES project_flags
            ON DELETE CASCADE,
    notification_id bigint    NOT NULL
        CONSTRAINT project_flag_notifications_notification_id_fkey
            REFERENCES notifications
            ON DELETE CASCADE,
    CONSTRAINT project_flag_notifications_notification_key
        UNIQUE (notification_id)
);

CREATE TABLE project_watchers
(
    project_id bigint NOT NULL
        CONSTRAINT project_watchers_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    user_id    bigint NOT NULL
        CONSTRAINT project_watchers_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT project_watchers_pkey
        PRIMARY KEY (project_id, user_id),
    CONSTRAINT project_watchers_project_id_user_id_key
        UNIQUE (project_id, user_id)
);

CREATE TABLE organizations
(
    id         bigint                   NOT NULL
        CONSTRAINT organizations_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    name       varchar(20)              NOT NULL
        CONSTRAINT organizations_name_key
            UNIQUE
        CONSTRAINT organizations_name_fkey
            REFERENCES users (name)
            ON DELETE CASCADE,
    owner_id   bigint                   NOT NULL
        CONSTRAINT organizations_owner_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    user_id    bigint
        CONSTRAINT organizations_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE
);

CREATE TABLE organization_members
(
    user_id         bigint NOT NULL
        CONSTRAINT organization_members_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    organization_id bigint NOT NULL
        CONSTRAINT organization_members_organization_id_fkey
            REFERENCES organizations
            ON DELETE CASCADE,
    hidden          bool DEFAULT FALSE,
    CONSTRAINT organization_members_pkey
        PRIMARY KEY (user_id, organization_id)
);

CREATE TABLE user_organization_roles
(
    id              bigserial                NOT NULL
        CONSTRAINT user_organization_roles_pkey
            PRIMARY KEY,
    created_at      timestamp WITH TIME ZONE NOT NULL,
    user_id         bigint                   NOT NULL
        CONSTRAINT user_organization_roles_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    role_type       varchar                  NOT NULL
        CONSTRAINT user_organization_roles_role_type_fkey
            REFERENCES roles (name)
            ON DELETE CASCADE,
    organization_id bigint                   NOT NULL
        CONSTRAINT user_organization_roles_organization_id_fkey
            REFERENCES organizations
            ON DELETE CASCADE,
    accepted        boolean DEFAULT FALSE    NOT NULL,
    CONSTRAINT organization_roles_user_id_organization_id UNIQUE (user_id, organization_id)
);

CREATE TABLE project_members
(
    project_id bigint NOT NULL
        CONSTRAINT project_members_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    user_id    bigint NOT NULL
        CONSTRAINT project_members_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT project_members_pkey
        PRIMARY KEY (project_id, user_id)
);

CREATE TABLE user_sign_ons
(
    id         bigserial                NOT NULL
        CONSTRAINT sign_ons_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    nonce      varchar(255)             NOT NULL
        CONSTRAINT sign_ons_nonce_key
            UNIQUE,
    completed  boolean DEFAULT FALSE    NOT NULL
);

CREATE TABLE project_version_unsafe_downloads
(
    id         bigserial                NOT NULL
        CONSTRAINT project_version_unsafe_downloads_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    user_id    bigint
        CONSTRAINT project_version_unsafe_downloads_fkey
            REFERENCES users
            ON DELETE CASCADE,
    address    inet                     NOT NULL
);

CREATE TABLE project_version_download_warnings
(
    id          bigserial                NOT NULL
        CONSTRAINT project_version_download_warnings_pkey
            PRIMARY KEY,
    created_at  timestamp WITH TIME ZONE NOT NULL,
    expires_at  timestamp WITH TIME ZONE NOT NULL,
    token       varchar(255)             NOT NULL,
    version_id  bigint                   NOT NULL
        CONSTRAINT project_version_download_warnings_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    address     inet                     NOT NULL,
    confirmed   boolean DEFAULT FALSE    NOT NULL,
    download_id bigint
        CONSTRAINT project_version_download_warnings_download_id_fkey
            REFERENCES project_version_unsafe_downloads
            ON DELETE CASCADE,
    CONSTRAINT project_version_download_warnings_address_key
        UNIQUE (address, version_id)
);

CREATE TABLE project_version_reviews
(
    id         bigserial                NOT NULL
        CONSTRAINT project_version_reviews_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    version_id bigint                   NOT NULL
        CONSTRAINT project_version_reviews_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    user_id    bigint                   NOT NULL
        CONSTRAINT project_version_reviews_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    ended_at   timestamp WITH TIME ZONE,
    CONSTRAINT project_version_reviews_unique
        UNIQUE (version_id, user_id)
);

CREATE TABLE project_version_review_messages
(
    id         bigserial                NOT NULL
        CONSTRAINT project_version_review_messages_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    review_id  bigint                   NOT NULL
        CONSTRAINT project_version_review_messages_review_id_fkey
            REFERENCES project_version_reviews
            ON DELETE CASCADE,
    message    text                     NOT NULL,
    args       jsonb                    NOT NULL,
    action     bigint                   NOT NULL
);

CREATE TABLE project_visibility_changes
(
    id          bigserial                              NOT NULL
        CONSTRAINT project_visibility_changes_pkey
            PRIMARY KEY,
    created_at  timestamp WITH TIME ZONE DEFAULT now() NOT NULL,
    created_by  bigint                                 NOT NULL
        CONSTRAINT project_visibility_changes_created_by_fkey
            REFERENCES users
            ON DELETE NO ACTION,
    project_id  bigint                                 NOT NULL
        CONSTRAINT project_visibility_changes_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    comment     text                                   NOT NULL,
    resolved_at timestamp WITH TIME ZONE,
    resolved_by bigint
        CONSTRAINT project_visibility_changes_resolved_by_fkey
            REFERENCES users
            ON DELETE SET NULL,
    visibility  integer                                NOT NULL
);

CREATE TABLE project_version_visibility_changes
(
    id          bigserial                              NOT NULL
        CONSTRAINT project_version_visibility_changes_pkey
            PRIMARY KEY,
    created_at  timestamp WITH TIME ZONE DEFAULT now() NOT NULL,
    created_by  bigint                                 NOT NULL
        CONSTRAINT project_version_visibility_changes_created_by_fkey
            REFERENCES users
            ON DELETE NO ACTION,
    version_id  bigint                                 NOT NULL
        CONSTRAINT project_version_visibility_changes_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    comment     text                                   NOT NULL,
    resolved_at timestamp WITH TIME ZONE,
    resolved_by bigint
        CONSTRAINT project_version_visibility_changes_resolved_by_fkey
            REFERENCES users
            ON DELETE SET NULL,
    visibility  integer                                NOT NULL
);

CREATE TABLE user_global_roles
(
    user_id bigint NOT NULL
        CONSTRAINT user_global_roles_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    role_id bigint NOT NULL
        CONSTRAINT user_global_roles_role_id_fkey
            REFERENCES roles
            ON DELETE CASCADE,
    CONSTRAINT user_global_roles_pkey
        PRIMARY KEY (user_id, role_id)
);

CREATE TABLE api_keys
(
    id                  bigserial                NOT NULL
        CONSTRAINT api_keys_pkey
            PRIMARY KEY,
    created_at          timestamp WITH TIME ZONE NOT NULL,
    name                varchar(255)             NOT NULL,
    owner_id            bigint                   NOT NULL
        CONSTRAINT api_keys_owner_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    token_identifier    varchar(255)             NOT NULL
        CONSTRAINT api_keys_token_identifier_key
            UNIQUE,
    token               text                     NOT NULL,
    raw_key_permissions bit(64)                  NOT NULL,
    CONSTRAINT api_keys_owner_id_token_identifier
        UNIQUE (owner_id, token_identifier),
    CONSTRAINT api_keys_owner_id_name_key
        UNIQUE (owner_id, name)
);

CREATE TABLE logged_actions_project
(
    id         bigserial                NOT NULL
        CONSTRAINT logged_actions_project_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    user_id    bigint
        CONSTRAINT logged_actions_project_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address    inet                     NOT NULL,
    action     logged_action_type       NOT NULL,
    project_id bigint
        CONSTRAINT logged_actions_project_project_id_fkey
            REFERENCES projects
            ON DELETE SET NULL,
    new_state  text                     NOT NULL,
    old_state  text                     NOT NULL
);

CREATE TABLE logged_actions_version
(
    id         bigserial                NOT NULL
        CONSTRAINT logged_actions_version_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    user_id    bigint
        CONSTRAINT logged_actions_version_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address    inet                     NOT NULL,
    action     logged_action_type       NOT NULL,
    project_id bigint
        CONSTRAINT logged_actions_version_project_id_fkey
            REFERENCES projects
            ON DELETE SET NULL,
    version_id bigint
        CONSTRAINT logged_actions_version_version_id_fkey
            REFERENCES project_versions
            ON DELETE SET NULL,
    new_state  text                     NOT NULL,
    old_state  text                     NOT NULL
);

CREATE TABLE logged_actions_page
(
    id         bigserial                NOT NULL
        CONSTRAINT logged_actions_page_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    user_id    bigint
        CONSTRAINT logged_actions_page_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address    inet                     NOT NULL,
    action     logged_action_type       NOT NULL,
    project_id bigint
        CONSTRAINT logged_actions_page_project_id_fkey
            REFERENCES projects
            ON DELETE SET NULL,
    page_id    bigint
        CONSTRAINT logged_actions_page_page_id_fkey
            REFERENCES project_pages
            ON DELETE SET NULL,
    new_state  text                     NOT NULL,
    old_state  text                     NOT NULL
);

CREATE TABLE logged_actions_user
(
    id         bigserial                NOT NULL
        CONSTRAINT logged_actions_user_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    user_id    bigint
        CONSTRAINT logged_actions_user_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address    inet                     NOT NULL,
    action     logged_action_type       NOT NULL,
    subject_id bigint
        CONSTRAINT logged_actions_user_subject_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    new_state  text                     NOT NULL,
    old_state  text                     NOT NULL
);

CREATE TABLE logged_actions_organization
(
    id              bigserial                NOT NULL
        CONSTRAINT logged_actions_organization_pkey
            PRIMARY KEY,
    created_at      timestamp WITH TIME ZONE NOT NULL,
    user_id         bigint
        CONSTRAINT logged_actions_organization_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address         inet                     NOT NULL,
    action          logged_action_type       NOT NULL,
    organization_id bigint
        CONSTRAINT logged_actions_organization_organization_id_fkey
            REFERENCES organizations
            ON DELETE SET NULL,
    new_state       text                     NOT NULL,
    old_state       text                     NOT NULL
);

CREATE TABLE project_versions_downloads_individual
(
    id         bigserial                NOT NULL
        CONSTRAINT project_versions_downloads_individual_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT project_versions_downloads_individual_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    version_id bigint                   NOT NULL
        CONSTRAINT project_versions_downloads_individual_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    address    inet                     NOT NULL,
    cookie     varchar(36)              NOT NULL,
    user_id    bigint
        CONSTRAINT project_versions_downloads_individual_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    processed  integer DEFAULT 0        NOT NULL
);

CREATE TABLE project_versions_downloads
(
    day        date    NOT NULL,
    project_id bigint  NOT NULL
        CONSTRAINT project_versions_downloads_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    version_id bigint  NOT NULL
        CONSTRAINT project_versions_downloads_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    downloads  integer NOT NULL,
    CONSTRAINT project_versions_downloads_pkey
        PRIMARY KEY (day, version_id)
);

CREATE INDEX project_versions_downloads_project_id_version_id_idx
    ON project_versions_downloads (project_id, version_id);

CREATE TABLE project_views_individual
(
    id         bigserial                NOT NULL
        CONSTRAINT project_views_individual_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT project_views_individual_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    address    inet                     NOT NULL,
    cookie     varchar(36)              NOT NULL,
    user_id    bigint
        CONSTRAINT project_views_individual_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    processed  integer DEFAULT 0        NOT NULL
);


CREATE TABLE project_views
(
    day        date    NOT NULL DEFAULT current_date,
    project_id bigint  NOT NULL
        CONSTRAINT project_views_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    views      integer NOT NULL DEFAULT 1,
    CONSTRAINT project_views_pkey
        PRIMARY KEY (project_id, day)
);

CREATE TABLE jobs
(
    id                    bigserial                NOT NULL
        CONSTRAINT jobs_pkey
            PRIMARY KEY,
    created_at            timestamp WITH TIME ZONE NOT NULL,
    last_updated          timestamp WITH TIME ZONE,
    retry_at              timestamp WITH TIME ZONE,
    last_error            text,
    last_error_descriptor text,
    state                 job_state                NOT NULL,
    job_type              text                     NOT NULL,
    job_properties        jsonb                    NOT NULL
);

CREATE TABLE user_refresh_tokens
(
    id           bigserial                NOT NULL
        CONSTRAINT user_refresh_token_pkey
            PRIMARY KEY,
    created_at   timestamp WITH TIME ZONE NOT NULL,
    user_id      bigint                   NOT NULL
        CONSTRAINT user_refresh_tokens_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    last_updated timestamp WITH TIME ZONE NOT NULL,
    token        uuid                     NOT NULL CONSTRAINT user_refresh_tokens_token_unique UNIQUE,
    device_id    uuid                     NOT NULL CONSTRAINT user_refresh_tokens_device_id_unique UNIQUE
);

CREATE TABLE user_oauth_token
(
    id         bigserial                NOT NULL
        CONSTRAINT user_oauth_token_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    username   varchar(255),
    id_token   text
);

CREATE INDEX user_refresh_tokens_token_idx ON user_refresh_tokens (token);

CREATE TABLE pinned_project_versions
(
    id         bigserial                NOT NULL
        CONSTRAINT project_pinned_versions_pkey
            PRIMARY KEY,
    created_at timestamp WITH TIME ZONE NOT NULL,
    project_id bigint                   NOT NULL
        CONSTRAINT project_pinned_versions_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    version_id bigint                   NOT NULL
        CONSTRAINT project_pinned_versions_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT pinned_project_versions_project_version_key
        UNIQUE (project_id, version_id)
);

CREATE TABLE pinned_user_projects
(
    id         bigserial NOT NULL
        CONSTRAINT project_pinned_pkey
            PRIMARY KEY,
    user_id    bigint    NOT NULL
        CONSTRAINT project_pinned_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    project_id bigint    NOT NULL
        CONSTRAINT project_pinned_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    CONSTRAINT pinned_projects_project_user_key
        UNIQUE (project_id, user_id)
);
