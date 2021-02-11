CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE role_category AS ENUM ('global', 'project', 'organization');

CREATE TYPE logged_action_type AS ENUM (
    'project_visibility_change',
    'project_renamed',
    'project_flagged',
    'project_settings_changed',
    'project_member_removed',
    'project_icon_changed',
    'project_flag_resolved',
    'project_page_created',
    'project_page_deleted',
    'project_page_edited',
    'version_deleted',
    'version_uploaded',
    'version_description_changed',
    'version_review_state_changed',
    'user_tagline_changed',
    'user_locked',
    'user_unlocked',
    'user_apikey_create',
    'user_apikey_delete',
    'org_members_added',
    'org_member_removed',
    'org_member_roles_updated'
    );

CREATE TYPE job_state AS ENUM ('not_started', 'started', 'done', 'fatal_failure');

CREATE TABLE users
(
    id bigint NOT NULL
        CONSTRAINT users_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    full_name varchar(255),
    name varchar(255) NOT NULL
        CONSTRAINT users_name_key
            UNIQUE,
    email varchar(255)
        CONSTRAINT users_email_key
            UNIQUE,
    tagline varchar(255),
    join_date timestamp with time zone,
    read_prompts integer[] DEFAULT '{}'::integer[] NOT NULL,
    locked boolean DEFAULT FALSE NOT NULL,
    language varchar(16)
);

CREATE TABLE projects
(
    id bigserial NOT NULL
        CONSTRAINT projects_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    name varchar(255) NOT NULL,
    slug varchar(255) NOT NULL,
        CONSTRAINT projects_namespace_unique UNIQUE (name, slug),
    owner_name varchar(255) NOT NULL
        CONSTRAINT projects_owner_name_fkey
            REFERENCES users (name)
            ON UPDATE CASCADE,
    owner_id bigint NOT NULL
        CONSTRAINT projects_owner_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    topic_id integer,
    post_id integer,
    category integer NOT NULL,
    description varchar(255),
    visibility integer default 1 NOT NULL,
    notes jsonb default '{}'::jsonb NOT NULL,
    keywords text[] default ARRAY[]::text[] NOT NULL,
    homepage varchar(255),
    issues varchar(255),
    source varchar(255),
    support varchar(255),
    license_name varchar(255),
    license_url varchar(255),
    forum_sync boolean DEFAULT TRUE NOT NULL,
    CONSTRAINT projects_owner_name_name_key
        UNIQUE (owner_name, name),
    CONSTRAINT projects_owner_name_slug_key
        UNIQUE (owner_name, slug)
);

CREATE INDEX projects_owner_id
    on projects (owner_id);

CREATE TABLE project_stars
(
    user_id bigint NOT NULL
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
    id bigserial NOT NULL
        CONSTRAINT pages_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT pages_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    name varchar(255) NOT NULL,
    slug varchar(255) NOT NULL,
    contents text default ''::text NOT NULL,
    deletable boolean DEFAULT TRUE NOT NULL,
    parent_id bigint
        CONSTRAINT project_pages_parent_id_fkey
            REFERENCES project_pages
            ON DELETE RESTRICT
);

CREATE INDEX page_slug_idx
    on project_pages (lower(slug::text));

CREATE INDEX page_parent_id_idx
    on project_pages (parent_id);

CREATE TABLE project_channels
(
    id bigserial NOT NULL
        CONSTRAINT channels_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    name varchar(255) NOT NULL,
    color integer NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT channels_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    non_reviewed boolean DEFAULT FALSE NOT NULL,
    CONSTRAINT channels_project_id_name_key
        UNIQUE (project_id, name),
    CONSTRAINT channels_project_id_color_id_key
        UNIQUE (project_id, color)
);

CREATE TABLE project_versions
(
    id bigserial NOT NULL
        CONSTRAINT versions_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    version_string varchar(255) NOT NULL,
    description text,
    project_id bigint NOT NULL
        CONSTRAINT versions_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    channel_id bigint NOT NULL
        CONSTRAINT versions_channel_id_fkey
            REFERENCES project_channels
            ON DELETE CASCADE,
    file_size bigint default 1
        CONSTRAINT versions_file_size_check
            CHECK (file_size > 0),
    hash varchar(32),
    file_name varchar(255),
    external_url varchar(255),
    reviewer_id bigint
        CONSTRAINT project_versions_reviewer_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    approved_at timestamp with time zone,
    author_id bigint
        CONSTRAINT project_versions_author_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    visibility integer DEFAULT 1 NOT NULL,
    review_state integer DEFAULT 0 NOT NULL,
    create_forum_post boolean NOT NULL,
    post_id integer
);

CREATE INDEX project_version_version_string_idx
    ON project_versions (version_string);

CREATE TABLE recommended_project_versions
(
    id bigserial NOT NULL
        CONSTRAINT recommended_project_versions_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    version_id bigint NOT NULL
        CONSTRAINT recommended_project_versions_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    project_id bigint NOT NULL
        CONSTRAINT recommended_project_versions_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    platform bigint NOT NULL,
    CONSTRAINT recommended_project_versions_unique
        UNIQUE (project_id, platform)
);

CREATE TABLE platform_versions
(
    id         bigserial                NOT NULL
        CONSTRAINT platform_versions_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    platform   bigint                   NOT NULL,
    version    varchar(255)             NOT NULL,
    CONSTRAINT platform_version_unique UNIQUE (platform, version)
);

CREATE TABLE project_version_dependencies
(
    id bigserial NOT NULL CONSTRAINT version_dependencies_pkey PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    version_id bigint NOT NULL,
    platform bigint NOT NULL,
    name text NOT NULL,
    required boolean NOT NULL,
    project_id bigint,
    external_url text,
    CONSTRAINT project_version_dependencies_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_dependencies_project_id_fkey
        FOREIGN KEY (project_id)
            REFERENCES projects
            ON DELETE SET NULL
);

CREATE TABLE project_version_platform_dependencies
(
    id bigserial NOT NULL CONSTRAINT version_platform_dependencies_pkey PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    version_id bigint NOT NULL,
    platform_version_id bigint,
    CONSTRAINT project_version_platform_dependencies_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_dependencies_platform_version_id_fkey
        FOREIGN KEY (platform_version_id)
            REFERENCES platform_versions
            ON DELETE CASCADE
);

CREATE TABLE roles
(
    id bigint NOT NULL
        CONSTRAINT roles_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    name varchar(255) NOT NULL,
    category role_category NOT NULL,
    title varchar(255) NOT NULL,
    color varchar(255) NOT NULL,
    assignable boolean NOT NULL,
    rank integer,
    permission bit(64) DEFAULT '0'::bit(64) NOT NULL
);

CREATE UNIQUE INDEX role_name_idx
    ON roles (name);

CREATE TABLE user_project_roles
(
    id bigserial NOT NULL
        constraint user_project_roles_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint NOT NULL
        CONSTRAINT user_project_roles_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    role_type varchar NOT NULL
        CONSTRAINT user_project_roles_role_type_fkey
            REFERENCES roles (name)
            ON DELETE CASCADE,
    project_id bigint NOT NULL
        CONSTRAINT user_project_roles_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    accepted boolean DEFAULT FALSE NOT NULL,
    CONSTRAINT user_project_roles_user_id_role_type_id_project_id_key
        UNIQUE (user_id, role_type, project_id)
);

CREATE TABLE project_flags
(
    id bigserial NOT NULL
        CONSTRAINT flags_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT flags_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    user_id bigint NOT NULL
        CONSTRAINT flags_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    reason integer NOT NULL,
    resolved boolean DEFAULT FALSE NOT NULL,
    comment text NOT NULL,
    resolved_at timestamp with time zone,
    resolved_by bigint
        CONSTRAINT project_flags_resolved_by_fkey
            REFERENCES users
            ON DELETE SET NULL
);

CREATE TABLE notifications
(
    id bigserial NOT NULL
        CONSTRAINT notifications_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint NOT NULL
        CONSTRAINT notifications_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    notification_type integer NOT NULL,
    action varchar(255),
    read boolean DEFAULT FALSE NOT NULL,
    origin_id bigint
        CONSTRAINT notifications_origin_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    message_args varchar(255) [] NOT NULL
);

CREATE TABLE project_watchers
(
    project_id bigint NOT NULL
        CONSTRAINT project_watchers_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    user_id bigint NOT NULL
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
    id bigint NOT NULL
        CONSTRAINT organizations_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    name varchar(20) NOT NULL
        CONSTRAINT organizations_name_key
            UNIQUE
        CONSTRAINT organizations_name_fkey
            REFERENCES users (name)
            ON DELETE CASCADE,
    owner_id bigint NOT NULL
        CONSTRAINT organizations_owner_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    user_id bigint
        CONSTRAINT organizations_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE
);

CREATE TABLE organization_members
(
    user_id bigint NOT NULL
        CONSTRAINT organization_members_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    organization_id bigint NOT NULL
        CONSTRAINT organization_members_organization_id_fkey
            REFERENCES organizations
            ON DELETE CASCADE,
    CONSTRAINT organization_members_pkey
        PRIMARY KEY (user_id, organization_id)
);

CREATE TABLE user_organization_roles
(
    id bigserial NOT NULL
        CONSTRAINT user_organization_roles_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint NOT NULL
        CONSTRAINT user_organization_roles_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    role_type varchar NOT NULL
        CONSTRAINT user_organization_roles_role_type_fkey
            REFERENCES roles (name)
            ON DELETE CASCADE,
    organization_id bigint NOT NULL
        CONSTRAINT user_organization_roles_organization_id_fkey
            REFERENCES organizations
            ON DELETE CASCADE,
    accepted boolean default false NOT NULL,
    CONSTRAINT user_organization_roles_user_id_role_type_id_organization_id_ke
        UNIQUE (user_id, role_type, organization_id)
);

CREATE TABLE project_members
(
    project_id bigint NOT NULL
        CONSTRAINT project_members_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    user_id bigint NOT NULL
        CONSTRAINT project_members_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT project_members_pkey
        PRIMARY KEY (project_id, user_id)
);

CREATE TABLE user_sign_ons
(
    id bigserial NOT NULL
        CONSTRAINT sign_ons_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    nonce varchar(255) NOT NULL
        CONSTRAINT sign_ons_nonce_key
            UNIQUE,
    completed boolean DEFAULT FALSE NOT NULL
);

CREATE TABLE project_version_unsafe_downloads
(
    id bigserial NOT NULL
        CONSTRAINT project_version_unsafe_downloads_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint
        CONSTRAINT project_version_unsafe_downloads_fkey
            REFERENCES users
            ON DELETE CASCADE,
    address inet NOT NULL,
    download_type integer NOT NULL
);

CREATE TABLE project_version_download_warnings
(
    id bigserial NOT NULL
        CONSTRAINT project_version_download_warnings_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    expiration timestamp with time zone NOT NULL,
    token varchar(255) NOT NULL,
    version_id bigint NOT NULL
        CONSTRAINT project_version_download_warnings_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    address inet NOT NULL,
    confirmed boolean DEFAULT FALSE NOT NULL,
    download_id bigint
        CONSTRAINT project_version_download_warnings_download_id_fkey
            REFERENCES project_version_unsafe_downloads
            ON DELETE CASCADE,
    CONSTRAINT project_version_download_warnings_address_key
        UNIQUE (address, version_id)
);

CREATE TABLE project_api_keys
(
    id bigserial NOT NULL
        CONSTRAINT project_api_keys_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT project_api_keys_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    value varchar(255) NOT NULL
);

CREATE TABLE project_version_reviews
(
    id bigserial NOT NULL
        CONSTRAINT project_version_reviews_pkey
            PRIMARY KEY,
    version_id bigint NOT NULL
        CONSTRAINT project_version_reviews_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    user_id bigint NOT NULL
        CONSTRAINT project_version_reviews_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    ended_at timestamp with time zone,
    comment jsonb DEFAULT '{}'::jsonb NOT NULL
);

CREATE TABLE project_visibility_changes
(
    id bigserial NOT NULL
        CONSTRAINT project_visibility_changes_pkey
            PRIMARY KEY,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by bigint NOT NULL
        CONSTRAINT project_visibility_changes_created_by_fkey
            REFERENCES users
            ON DELETE CASCADE,
    project_id bigint NOT NULL
        CONSTRAINT project_visibility_changes_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    comment text NOT NULL,
    resolved_at timestamp with time zone,
    resolved_by bigint
        CONSTRAINT project_visibility_changes_resolved_by_fkey
            REFERENCES users
            ON DELETE CASCADE,
    visibility integer NOT NULL
);

CREATE TABLE project_version_visibility_changes
(
    id bigserial NOT NULL
        CONSTRAINT project_version_visibility_changes_pkey
            PRIMARY KEY,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by bigint NOT NULL
        CONSTRAINT project_version_visibility_changes_created_by_fkey
            REFERENCES users
            ON DELETE CASCADE,
    version_id bigint NOT NULL
        CONSTRAINT project_version_visibility_changes_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    comment text NOT NULL,
    resolved_at timestamp with time zone,
    resolved_by bigint
        CONSTRAINT project_version_visibility_changes_resolved_by_fkey
            REFERENCES users
            ON DELETE CASCADE,
    visibility integer NOT NULL
);

CREATE TABLE project_version_tags
(
    id bigserial NOT NULL
        CONSTRAINT project_version_tags_pkey
            PRIMARY KEY,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    version_id bigint NOT NULL
        CONSTRAINT project_version_tags_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    name varchar(255) NOT NULL,
    data varchar(255)[],
    color integer NOT NULL
);

CREATE INDEX projects_versions_tags_version_id
    on project_version_tags (version_id);

CREATE INDEX project_version_tags_name_data_idx
    on project_version_tags (name, data);

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
    id bigserial NOT NULL
        CONSTRAINT api_keys_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    name varchar(255) NOT NULL,
    owner_id bigint NOT NULL
        CONSTRAINT api_keys_owner_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    token_identifier varchar(255) NOT NULL
        CONSTRAINT api_keys_token_identifier_key
            UNIQUE,
    token text NOT NULL,
    raw_key_permissions bit(64) NOT NULL,
    CONSTRAINT api_keys_owner_id_name_key
        UNIQUE (owner_id, name)
);

CREATE TABLE api_sessions
(
    id bigserial NOT NULL
        constraint api_sessions_pkey
            primary key,
    created_at timestamp with time zone NOT NULL,
    token varchar(255) NOT NULL,
    key_id bigint
        constraint api_sessions_key_id_fkey
            references api_keys
            ON DELETE CASCADE,
    user_id bigint
        constraint api_sessions_user_id_fkey
            references users
            ON DELETE CASCADE,
    expires timestamp with time zone NOT NULL
);

CREATE TABLE logged_actions_project
(
    id bigserial NOT NULL
        CONSTRAINT logged_actions_project_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint
        CONSTRAINT logged_actions_project_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address inet NOT NULL,
    action logged_action_type NOT NULL,
    project_id bigint
        CONSTRAINT logged_actions_project_project_id_fkey
            REFERENCES projects
            ON DELETE SET NULL,
    new_state text NOT NULL,
    old_state text NOT NULL
);

CREATE TABLE logged_actions_version
(
    id bigserial NOT NULL
        CONSTRAINT logged_actions_version_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint
        CONSTRAINT logged_actions_version_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address inet NOT NULL,
    action logged_action_type NOT NULL,
    project_id bigint
        CONSTRAINT logged_actions_version_project_id_fkey
            REFERENCES projects
            ON DELETE SET NULL,
    version_id bigint
        CONSTRAINT logged_actions_version_version_id_fkey
            REFERENCES project_versions
            ON DELETE SET NULL,
    new_state text NOT NULL,
    old_state text NOT NULL
);

CREATE TABLE logged_actions_page
(
    id bigserial NOT NULL
        CONSTRAINT logged_actions_page_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint
        CONSTRAINT logged_actions_page_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address inet NOT NULL,
    action logged_action_type NOT NULL,
    project_id bigint
        CONSTRAINT logged_actions_page_project_id_fkey
            REFERENCES projects
            ON DELETE SET NULL,
    page_id bigint
        CONSTRAINT logged_actions_page_page_id_fkey
            REFERENCES project_pages
            ON DELETE SET NULL,
    new_state text NOT NULL,
    old_state text NOT NULL
);

CREATE TABLE logged_actions_user
(
    id bigserial NOT NULL
        CONSTRAINT logged_actions_user_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint
        CONSTRAINT logged_actions_user_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address inet NOT NULL,
    action logged_action_type NOT NULL,
    subject_id bigint
        CONSTRAINT logged_actions_user_subject_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    new_state text NOT NULL,
    old_state text NOT NULL
);

CREATE TABLE logged_actions_organization
(
    id bigserial NOT NULL
        CONSTRAINT logged_actions_organization_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint
        CONSTRAINT logged_actions_organization_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    address inet NOT NULL,
    action logged_action_type NOT NULL,
    organization_id bigint
        CONSTRAINT logged_actions_organization_organization_id_fkey
            REFERENCES organizations
            ON DELETE SET NULL,
    new_state text NOT NULL,
    old_state text NOT NULL
);

CREATE TABLE project_versions_downloads_individual
(
    id bigserial NOT NULL
        CONSTRAINT project_versions_downloads_individual_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT project_versions_downloads_individual_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    version_id bigint NOT NULL
        CONSTRAINT project_versions_downloads_individual_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    address inet NOT NULL,
    cookie varchar(36) NOT NULL,
    user_id bigint
        CONSTRAINT project_versions_downloads_individual_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    processed integer DEFAULT 0 NOT NULL
);

CREATE TABLE project_versions_downloads
(
    day date NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT project_versions_downloads_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    version_id bigint NOT NULL
        CONSTRAINT project_versions_downloads_version_id_fkey
            REFERENCES project_versions
            ON DELETE CASCADE,
    downloads integer NOT NULL,
    CONSTRAINT project_versions_downloads_pkey
        PRIMARY KEY (day, version_id)
);

CREATE INDEX project_versions_downloads_project_id_version_id_idx
    on project_versions_downloads (project_id, version_id);

CREATE TABLE project_views_individual
(
    id bigserial NOT NULL
        CONSTRAINT project_views_individual_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT project_views_individual_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    address inet NOT NULL,
    cookie varchar(36) NOT NULL,
    user_id bigint
        CONSTRAINT project_views_individual_user_id_fkey
            REFERENCES users
            ON DELETE SET NULL,
    processed integer DEFAULT 0 NOT NULL
);


CREATE TABLE project_views
(
    day date NOT NULL DEFAULT current_date,
    project_id bigint NOT NULL
        CONSTRAINT project_views_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    views integer NOT NULL DEFAULT 1,
    CONSTRAINT project_views_pkey
        PRIMARY KEY (project_id, day)
);

CREATE TABLE jobs
(
    id bigserial NOT NULL
        CONSTRAINT jobs_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    last_updated timestamp with time zone,
    retry_at timestamp with time zone,
    last_error text,
    last_error_descriptor text,
    state job_state NOT NULL,
    job_type text NOT NULL,
    job_properties hstore NOT NULL
);

CREATE TABLE user_refresh_tokens
(
    id bigserial NOT NULL
        CONSTRAINT user_refresh_token_pkey
            PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    user_id bigint NOT NULL
        CONSTRAINT user_refresh_tokens_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    last_updated timestamp with time zone NOT NULL,
    token uuid NOT NULL CONSTRAINT user_refresh_tokens_token_unique UNIQUE,
    device_id uuid NOT NULL CONSTRAINT user_refresh_tokens_device_id_unique UNIQUE
);

CREATE INDEX user_refresh_tokens_token_idx ON user_refresh_tokens (token);

CREATE VIEW project_members_all(id, user_id) AS
SELECT p.id,
       pm.user_id
FROM projects p
         LEFT JOIN project_members pm ON p.id = pm.project_id
UNION
SELECT p.id,
       om.user_id
FROM projects p
         LEFT JOIN organization_members om ON p.owner_id = om.organization_id
WHERE om.user_id IS NOT NULL;

CREATE MATERIALIZED VIEW home_projects AS
WITH tags AS (
    SELECT sq.version_id,
           sq.project_id,
           sq.version_string,
           sq.tag_name,
           sq.tag_version,
           sq.tag_color
    FROM (SELECT pv.id                                                                                            AS version_id,
                 pv.project_id,
                 pv.version_string,
                 pvt.name                                                                                         AS tag_name,
                 pvt.data                                                                                         AS tag_version,
                 pvt.platform_version,
                 pvt.color                                                                                        AS tag_color,
                 row_number()
                 OVER (PARTITION BY pv.project_id, pvt.platform_version ORDER BY pv.created_at DESC)              AS row_num
          FROM project_versions pv
                   JOIN (SELECT pvti.version_id,
                                pvti.name,
                                pvti.data,
                                CASE
                                    WHEN pvti.name::text = 'Paper'::text THEN array_to_string(pvti.data, ', ')
                                    WHEN pvti.name::text = 'Waterfall'::text THEN array_to_string(pvti.data, ', ')
                                    WHEN pvti.name::text = 'Velocity'::text THEN array_to_string(pvti.data, ', ')
                                    ELSE NULL::text
                                    END AS platform_version,
                                pvti.color
                         FROM project_version_tags pvti
                         WHERE (pvti.name::text = ANY
                                (ARRAY ['Paper'::character varying, 'Waterfall'::character varying, 'Velocity'::character varying]::text[]))
                           AND pvti.data IS NOT NULL) pvt ON pv.id = pvt.version_id
          WHERE pv.visibility = 0
            AND (pvt.name::text = ANY
                 (ARRAY ['Paper'::character varying, 'Waterfall'::character varying, 'Velocity'::character varying]::text[]))
            AND pvt.platform_version IS NOT NULL) sq
    WHERE sq.row_num = 1
    ORDER BY sq.platform_version DESC
)
SELECT p.id,
       p.owner_name,
       array_agg(DISTINCT pm.user_id)            AS project_members,
       p.slug,
       p.visibility,
       COALESCE(pva.views, 0::bigint)            AS views,
       COALESCE(pda.downloads, 0::bigint)        AS downloads,
       COALESCE(pvr.recent_views, 0::bigint)     AS recent_views,
       COALESCE(pdr.recent_downloads, 0::bigint) AS recent_downloads,
       COALESCE(ps.stars, 0::bigint)             AS stars,
       COALESCE(pw.watchers, 0::bigint)          AS watchers,
       p.category,
       p.description,
       p.name,
       p.created_at,
       max(lv.created_at)                        AS last_updated,
       to_jsonb(ARRAY(SELECT jsonb_build_object('version_string', tags.version_string || '.' || tags.version_id, 'tag_name', tags.tag_name,
                                                'tag_version', tags.tag_version, 'tag_color',
                                                tags.tag_color) AS jsonb_build_object
                      FROM tags
                      WHERE tags.project_id = p.id
                      LIMIT 5))                  AS promoted_versions,
       ((setweight((to_tsvector('english'::regconfig, p.name::text) ||
                    to_tsvector('english'::regconfig, regexp_replace(p.name::text, '([a-z])([A-Z]+)'::text,
                                                                     '\1_\2'::text, 'g'::text))), 'A'::"char") ||
         setweight(to_tsvector('english'::regconfig, p.description::text), 'B'::"char")) ||
        setweight(to_tsvector('english'::regconfig, array_to_string(p.keywords, ' '::text)), 'C'::"char")) || setweight(
                   to_tsvector('english'::regconfig, p.owner_name::text) || to_tsvector('english'::regconfig,
                                                                                        regexp_replace(
                                                                                                p.owner_name::text,
                                                                                                '([a-z])([A-Z]+)'::text,
                                                                                                '\1_\2'::text,
                                                                                                'g'::text)),
                   'D'::"char")                  AS search_words
FROM projects p
         LEFT JOIN project_versions lv ON p.id = lv.project_id
         JOIN project_members_all pm ON p.id = pm.id
         LEFT JOIN (SELECT p_1.id,
                           COUNT(ps_1.user_id) AS stars
                    FROM projects p_1
                             LEFT JOIN project_stars ps_1 ON p_1.id = ps_1.project_id
                    GROUP BY p_1.id) ps ON p.id = ps.id
         LEFT JOIN (SELECT p_1.id,
                           count(pw_1.user_id) AS watchers
                    FROM projects p_1
                             LEFT JOIN project_watchers pw_1 ON p_1.id = pw_1.project_id
                    GROUP BY p_1.id) pw ON p.id = pw.id
         LEFT JOIN (SELECT pv.project_id,
                           sum(pv.views) AS views
                    FROM project_views pv
                    GROUP BY pv.project_id) pva ON p.id = pva.project_id
         LEFT JOIN (SELECT pv.project_id,
                           sum(pv.downloads) AS downloads
                    FROM project_versions_downloads pv
                    GROUP BY pv.project_id) pda ON p.id = pda.project_id
         LEFT JOIN (SELECT pv.project_id,
                           sum(pv.views) AS recent_views
                    FROM project_views pv
                    WHERE pv.day >= (CURRENT_DATE - '30 days'::interval)
                      AND pv.day <= CURRENT_DATE
                    GROUP BY pv.project_id) pvr ON p.id = pvr.project_id
         LEFT JOIN (SELECT pv.project_id,
                           sum(pv.downloads) AS recent_downloads
                    FROM project_versions_downloads pv
                    WHERE pv.day >= (CURRENT_DATE - '30 days'::interval)
                      AND pv.day <= CURRENT_DATE
                    GROUP BY pv.project_id) pdr ON p.id = pdr.project_id
GROUP BY p.id, ps.stars, pw.watchers, pva.views, pda.downloads, pvr.recent_views, pdr.recent_downloads;

CREATE VIEW global_trust(user_id, permission) AS
SELECT gr.user_id,
       COALESCE(bit_or(r.permission), '0'::bit(64)) AS permission
FROM user_global_roles gr
         JOIN roles r ON gr.role_id = r.id
GROUP BY gr.user_id;

CREATE VIEW project_trust(project_id, user_id, permission) AS
SELECT pm.project_id,
       pm.user_id,
       COALESCE(bit_or(r.permission), '0'::bit(64)) AS permission
FROM project_members pm
         JOIN user_project_roles rp ON pm.project_id = rp.project_id AND pm.user_id = rp.user_id AND rp.accepted
         JOIN roles r ON rp.role_type::text = r.name::text
GROUP BY pm.project_id, pm.user_id;

CREATE VIEW organization_trust(organization_id, user_id, permission) AS
SELECT om.organization_id,
       om.user_id,
       COALESCE(bit_or(r.permission), '0'::bit(64)) AS permission
FROM organization_members om
         JOIN user_organization_roles ro
              ON om.organization_id = ro.organization_id AND om.user_id = ro.user_id AND ro.accepted
         JOIN roles r ON ro.role_type::text = r.name::text
GROUP BY om.organization_id, om.user_id;

CREATE VIEW v_logged_actions(id, created_at, user_id, user_name, address, action, context_type, new_state, old_state, p_id, p_slug, p_owner_name, pv_id, pv_version_string, pp_id, pp_name, pp_slug, s_id, s_name) AS
SELECT a.id,
       a.created_at,
       a.user_id,
       u.name                       AS user_name,
       a.address,
       a.action,
       0                            AS context_type,
       a.new_state,
       a.old_state,
       p.id                         AS p_id,
       p.slug                       AS p_slug,
       ou.name                      AS p_owner_name,
       NULL::bigint                 AS pv_id,
       NULL::character varying(255) AS pv_version_string,
       NULL::bigint                 AS pp_id,
       NULL::character varying(255) AS pp_name,
       NULL::character varying(255) AS pp_slug,
       NULL::bigint                 AS s_id,
       NULL::character varying(255) AS s_name
FROM logged_actions_project a
         LEFT JOIN users u ON a.user_id = u.id
         LEFT JOIN projects p ON a.project_id = p.id
         LEFT JOIN users ou ON p.owner_id = ou.id
UNION ALL
SELECT a.id,
       a.created_at,
       a.user_id,
       u.name                  AS user_name,
       a.address,
       a.action,
       1                       AS context_type,
       a.new_state,
       a.old_state,
       p.id                    AS p_id,
       p.slug                  AS p_slug,
       ou.name                 AS p_owner_name,
       pv.id                   AS pv_id,
       pv.version_string       AS pv_version_string,
       NULL::bigint            AS pp_id,
       NULL::character varying AS pp_name,
       NULL::character varying AS pp_slug,
       NULL::bigint            AS s_id,
       NULL::character varying AS s_name
FROM logged_actions_version a
         LEFT JOIN users u ON a.user_id = u.id
         LEFT JOIN project_versions pv ON a.version_id = pv.id
         LEFT JOIN projects p ON a.project_id = p.id
         LEFT JOIN users ou ON p.owner_id = ou.id
UNION ALL
SELECT a.id,
       a.created_at,
       a.user_id,
       u.name                  AS user_name,
       a.address,
       a.action,
       2                       AS context_type,
       a.new_state,
       a.old_state,
       p.id                    AS p_id,
       p.slug                  AS p_slug,
       ou.name                 AS p_owner_name,
       NULL::bigint            AS pv_id,
       NULL::character varying AS pv_version_string,
       pp.id                   AS pp_id,
       pp.name                 AS pp_name,
       pp.slug                 AS pp_slug,
       NULL::bigint            AS s_id,
       NULL::character varying AS s_name
FROM logged_actions_page a
         LEFT JOIN users u ON a.user_id = u.id
         LEFT JOIN project_pages pp ON a.page_id = pp.id
         LEFT JOIN projects p ON a.project_id = p.id
         LEFT JOIN users ou ON p.owner_id = ou.id
UNION ALL
SELECT a.id,
       a.created_at,
       a.user_id,
       u.name                  AS user_name,
       a.address,
       a.action,
       3                       AS context_type,
       a.new_state,
       a.old_state,
       NULL::bigint            AS p_id,
       NULL::character varying AS p_slug,
       NULL::character varying AS p_owner_name,
       NULL::bigint            AS pv_id,
       NULL::character varying AS pv_version_string,
       NULL::bigint            AS pp_id,
       NULL::character varying AS pp_name,
       NULL::character varying AS pp_slug,
       s.id                    AS s_id,
       s.name                  AS s_name
FROM logged_actions_user a
         LEFT JOIN users u ON a.user_id = u.id
         LEFT JOIN users s ON a.subject_id = s.id
UNION ALL
SELECT a.id,
       a.created_at,
       a.user_id,
       u.name                  AS user_name,
       a.address,
       a.action,
       4                       AS context_type,
       a.new_state,
       a.old_state,
       NULL::bigint            AS p_id,
       NULL::character varying AS p_slug,
       NULL::character varying AS p_owner_name,
       NULL::bigint            AS pv_id,
       NULL::character varying AS pv_version_string,
       NULL::bigint            AS pp_id,
       NULL::character varying AS pp_name,
       NULL::character varying AS pp_slug,
       s.id                    AS s_id,
       s.name                  AS s_name
FROM logged_actions_organization a
         LEFT JOIN organizations o ON a.organization_id = o.id
         LEFT JOIN users u ON a.user_id = u.id
         LEFT JOIN users s ON o.user_id = s.id;

CREATE FUNCTION delete_old_project_version_download_warnings() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM project_version_download_warnings WHERE created_at < current_date - interval '30' day;
    RETURN NEW;
END
$$;

CREATE TRIGGER clean_old_project_version_download_warnings
    AFTER INSERT
    ON project_version_download_warnings
EXECUTE PROCEDURE delete_old_project_version_download_warnings();

CREATE FUNCTION delete_old_project_version_unsafe_downloads() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM project_version_unsafe_downloads WHERE created_at < current_date - interval '30' day;
    RETURN NEW;
END
$$;

CREATE TRIGGER clean_old_project_version_unsafe_downloads
    AFTER INSERT
    ON project_version_unsafe_downloads
EXECUTE PROCEDURE delete_old_project_version_unsafe_downloads();

CREATE FUNCTION update_project_name_trigger() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE projects p SET name = u.name FROM users u WHERE p.id = new.id AND u.id = new.owner_id;
END;
$$;

CREATE TRIGGER project_owner_name_updater
    AFTER UPDATE
        OF owner_id
    ON projects
    FOR EACH ROW
    WHEN  (old.owner_id <> new.owner_id)
EXECUTE PROCEDURE update_project_name_trigger();

CREATE FUNCTION logged_action_type_from_int(id integer) RETURNS logged_action_type
    IMMUTABLE
    STRICT
    LANGUAGE plpgsql
AS $$
BEGIN
    CASE id
        WHEN 0 THEN RETURN 'project_visibility_change';
        WHEN 2 THEN RETURN 'project_renamed';
        WHEN 3 THEN RETURN 'project_flagged';
        WHEN 4 THEN RETURN 'project_settings_changed';
        WHEN 5 THEN RETURN 'project_member_removed';
        WHEN 6 THEN RETURN 'project_icon_changed';
        WHEN 7 THEN RETURN 'project_page_edited';
        WHEN 13 THEN RETURN 'project_flag_resolved';
        WHEN 8 THEN RETURN 'version_deleted';
        WHEN 9 THEN RETURN 'version_uploaded';
        WHEN 12 THEN RETURN 'version_description_changed';
        WHEN 17 THEN RETURN 'version_review_state_changed';
        WHEN 14 THEN RETURN 'user_tagline_changed';
        ELSE
        END CASE;

    RETURN NULL;
END;
$$;

CREATE FUNCTION websearch_to_tsquery_postfix(dictionary regconfig, query text) RETURNS tsquery
    IMMUTABLE
    STRICT
    LANGUAGE plpgsql
AS $$
DECLARE
    arr  TEXT[]  := regexp_split_to_array(query, '\s+');
    last TEXT    := websearch_to_tsquery('simple', arr[array_length(arr, 1)])::TEXT;
    init TSQUERY := websearch_to_tsquery(dictionary, regexp_replace(query, '\S+$', ''));
BEGIN
    IF last = '' THEN
        BEGIN
            RETURN init && $2::TSQUERY;
        EXCEPTION
            WHEN SYNTAX_ERROR THEN
                RETURN init && websearch_to_tsquery('');
        END;
    END IF;

    RETURN init && (websearch_to_tsquery(dictionary, last) || to_tsquery('simple', last || ':*'));
END;
$$;
