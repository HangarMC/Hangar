create extension hstore;
create extension pgcrypto;
create role hangar with login password 'hangar';

create type role_category as enum ('global', 'project', 'organization');

alter type role_category owner to hangar;

create type logged_action_type as enum ('project_visibility_change', 'project_renamed', 'project_flagged', 'project_settings_changed', 'project_member_removed', 'project_icon_changed', 'project_page_edited', 'project_flag_resolved', 'version_deleted', 'version_uploaded', 'version_description_changed', 'version_review_state_changed', 'user_tagline_changed');

alter type logged_action_type owner to hangar;

create type job_state as enum ('not_started', 'started', 'done', 'fatal_failure');

alter type job_state owner to hangar;

create table users
(
    id bigint not null
        constraint users_pkey
            primary key,
    created_at timestamp with time zone not null,
    full_name varchar(255),
    name varchar(255) not null
        constraint users_name_key
            unique,
    email varchar(255)
        constraint users_email_key
            unique,
    tagline varchar(255),
    join_date timestamp with time zone,
    read_prompts integer[] default '{}'::integer[] not null,
    is_locked boolean default false not null,
    language varchar(16)
);

alter table users owner to hangar;

create table projects
(
    id bigserial not null
        constraint projects_pkey
            primary key,
    created_at timestamp with time zone not null,
    plugin_id varchar(255) not null
        constraint projects_plugin_id_key
            unique,
    name varchar(255) not null,
    slug varchar(255) not null,
    owner_name varchar(255) not null
        constraint projects_owner_name_fkey
            references users (name)
            on update cascade,
    recommended_version_id bigint,
    owner_id bigint not null
        constraint projects_owner_id_fkey
            references users
            on delete cascade,
    topic_id integer,
    post_id integer,
    category integer not null,
    description varchar(255),
    visibility integer default 1 not null,
    notes jsonb default '{}'::jsonb not null,
    keywords text[] default ARRAY[]::text[] not null,
    homepage varchar(255),
    issues varchar(255),
    source varchar(255),
    support varchar(255),
    license_name varchar(255),
    license_url varchar(255),
    forum_sync boolean default true not null,
    constraint projects_owner_name_name_key
        unique (owner_name, name),
    constraint projects_owner_name_slug_key
        unique (owner_name, slug)
);

alter table projects owner to hangar;

create index projects_recommended_version_id
    on projects (recommended_version_id);

create index projects_owner_id
    on projects (owner_id);

create table project_stars
(
    user_id bigint not null
        constraint project_stars_user_id_fkey
            references users
            on delete cascade,
    project_id bigint not null
        constraint project_stars_project_id_fkey
            references projects
            on delete cascade,
    constraint project_stars_pkey
        primary key (user_id, project_id)
);

alter table project_stars owner to hangar;

create table project_pages
(
    id bigserial not null
        constraint pages_pkey
            primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null
        constraint pages_project_id_fkey
            references projects
            on delete cascade,
    name varchar(255) not null,
    slug varchar(255) not null,
    contents text default ''::text not null,
    is_deletable boolean default true not null,
    parent_id bigint
        constraint project_pages_parent_id_fkey
            references project_pages
            on delete set null
);

alter table project_pages owner to hangar;

create index page_slug_idx
    on project_pages (lower(slug::text));

create index page_parent_id_idx
    on project_pages (parent_id);

create table project_channels
(
    id bigserial not null
        constraint channels_pkey
            primary key,
    created_at timestamp with time zone not null,
    name varchar(255) not null,
    color integer not null,
    project_id bigint not null
        constraint channels_project_id_fkey
            references projects
            on delete cascade,
    is_non_reviewed boolean default false not null,
    constraint channels_project_id_name_key
        unique (project_id, name),
    constraint channels_project_id_color_id_key
        unique (project_id, color)
);

alter table project_channels owner to hangar;

create table project_versions
(
    id bigserial not null
        constraint versions_pkey
            primary key,
    created_at timestamp with time zone not null,
    version_string varchar(255) not null,
    dependencies varchar(255) [] not null,
    description text,
    project_id bigint not null
        constraint versions_project_id_fkey
            references projects
            on delete cascade,
    channel_id bigint not null
        constraint versions_channel_id_fkey
            references project_channels
            on delete cascade,
    file_size bigint default 1 not null
        constraint versions_file_size_check
            check (file_size > 0),
    hash varchar(32) not null,
    file_name varchar(255) not null,
    reviewer_id bigint
        constraint project_versions_reviewer_id_fkey
            references users
            on delete set null,
    approved_at timestamp with time zone,
    author_id bigint
        constraint project_versions_author_id_fkey
            references users
            on delete set null,
    visibility integer default 1 not null,
    review_state integer default 0 not null,
    create_forum_post boolean not null,
    post_id integer
);

alter table project_versions owner to hangar;

alter table projects
    add constraint projects_recommended_version_id_fkey
        foreign key (recommended_version_id) references project_versions
            on delete set null;

create unique index versions_project_id_version_string_idx
    on project_versions (project_id, version_string);

create table user_project_roles
(
    id bigserial not null
        constraint user_project_roles_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint not null
        constraint user_project_roles_user_id_fkey
            references users
            on delete cascade,
    role_type varchar not null,
    project_id bigint not null
        constraint user_project_roles_project_id_fkey
            references projects
            on delete cascade,
    is_accepted boolean default false not null,
    constraint user_project_roles_user_id_role_type_id_project_id_key
        unique (user_id, role_type, project_id)
);

alter table user_project_roles owner to hangar;

create table project_flags
(
    id bigserial not null
        constraint flags_pkey
            primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null
        constraint flags_project_id_fkey
            references projects
            on delete cascade,
    user_id bigint not null
        constraint flags_user_id_fkey
            references users
            on delete cascade,
    reason integer not null,
    is_resolved boolean default false not null,
    comment text not null,
    resolved_at timestamp with time zone,
    resolved_by bigint
        constraint project_flags_resolved_by_fkey
            references users
            on delete set null
);

alter table project_flags owner to hangar;

create table notifications
(
    id bigserial not null
        constraint notifications_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint not null
        constraint notifications_user_id_fkey
            references users
            on delete cascade,
    notification_type integer not null,
    action varchar(255),
    read boolean default false not null,
    origin_id bigint
        constraint notifications_origin_id_fkey
            references users
            on delete set null,
    message_args varchar(255) [] not null
);

alter table notifications owner to hangar;

create table project_watchers
(
    project_id bigint not null
        constraint project_watchers_project_id_fkey
            references projects
            on delete cascade,
    user_id bigint not null
        constraint project_watchers_user_id_fkey
            references users
            on delete cascade,
    constraint project_watchers_pkey
        primary key (project_id, user_id),
    constraint project_watchers_project_id_user_id_key
        unique (project_id, user_id)
);

alter table project_watchers owner to hangar;

create table organizations
(
    id bigserial not null
        constraint organizations_pkey
            primary key,
    created_at timestamp with time zone not null,
    name varchar(20) not null
        constraint organizations_name_key
            unique
        constraint organizations_name_fkey
            references users (name)
            on update cascade,
    owner_id bigint not null
        constraint organizations_owner_id_fkey
            references users
            on delete cascade,
    user_id bigint
        constraint organizations_user_id_fkey
            references users
            on delete cascade
);

alter table organizations owner to hangar;

create table organization_members
(
    user_id bigint not null
        constraint organization_members_user_id_fkey
            references users
            on delete cascade,
    organization_id bigint not null
        constraint organization_members_organization_id_fkey
            references organizations
            on delete cascade,
    constraint organization_members_pkey
        primary key (user_id, organization_id)
);

alter table organization_members owner to hangar;

create table user_organization_roles
(
    id bigserial not null
        constraint user_organization_roles_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint not null
        constraint user_organization_roles_user_id_fkey
            references users
            on delete cascade,
    role_type varchar not null,
    organization_id bigint not null
        constraint user_organization_roles_organization_id_fkey
            references organizations
            on delete cascade,
    is_accepted boolean default false not null,
    constraint user_organization_roles_user_id_role_type_id_organization_id_ke
        unique (user_id, role_type, organization_id)
);

alter table user_organization_roles owner to hangar;

create table project_members
(
    project_id bigint not null
        constraint project_members_project_id_fkey
            references projects
            on delete cascade,
    user_id bigint not null
        constraint project_members_user_id_fkey
            references users
            on delete cascade,
    constraint project_members_pkey
        primary key (project_id, user_id)
);

alter table project_members owner to hangar;

create table user_sessions
(
    id bigserial not null
        constraint sessions_pkey
            primary key,
    created_at timestamp with time zone not null,
    expiration timestamp with time zone not null,
    token varchar(255) not null,
    user_id bigint not null
        constraint user_sessions_user_id_fkey
            references users
            on delete cascade
);

alter table user_sessions owner to hangar;

create index user_session_token_idx
    on user_sessions (token);

create table user_sign_ons
(
    id bigserial not null
        constraint sign_ons_pkey
            primary key,
    created_at timestamp with time zone not null,
    nonce varchar(255) not null
        constraint sign_ons_nonce_key
            unique,
    is_completed boolean default false not null
);

alter table user_sign_ons owner to hangar;

create table project_version_unsafe_downloads
(
    id bigserial not null
        constraint project_version_unsafe_downloads_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint
        constraint project_version_unsafe_downloads_fkey
            references users
            on delete cascade,
    address inet not null,
    download_type integer not null
);

alter table project_version_unsafe_downloads owner to hangar;

create table project_version_download_warnings
(
    id bigserial not null
        constraint project_version_download_warnings_pkey
            primary key,
    created_at timestamp with time zone not null,
    expiration timestamp with time zone not null,
    token varchar(255) not null,
    version_id bigint not null
        constraint project_version_download_warnings_version_id_fkey
            references project_versions
            on delete cascade,
    address inet not null,
    is_confirmed boolean default false not null,
    download_id bigint
        constraint project_version_download_warnings_download_id_fkey
            references project_version_unsafe_downloads
            on delete cascade,
    constraint project_version_download_warnings_address_key
        unique (address, version_id)
);

alter table project_version_download_warnings owner to hangar;

create table project_api_keys
(
    id bigserial not null
        constraint project_api_keys_pkey
            primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null
        constraint project_api_keys_project_id_fkey
            references projects
            on delete cascade,
    value varchar(255) not null
);

alter table project_api_keys owner to hangar;

create table project_version_reviews
(
    id bigserial not null
        constraint project_version_reviews_pkey
            primary key,
    version_id bigint not null
        constraint project_version_reviews_version_id_fkey
            references project_versions
            on delete cascade,
    user_id bigint not null
        constraint project_version_reviews_user_id_fkey
            references users
            on delete cascade,
    created_at timestamp with time zone default now() not null,
    ended_at timestamp with time zone,
    comment jsonb default '{}'::jsonb not null
);

alter table project_version_reviews owner to hangar;

create table project_visibility_changes
(
    id bigserial not null
        constraint project_visibility_changes_pkey
            primary key,
    created_at timestamp with time zone default now() not null,
    created_by bigint not null
        constraint project_visibility_changes_created_by_fkey
            references users
            on delete cascade,
    project_id bigint not null
        constraint project_visibility_changes_project_id_fkey
            references projects
            on delete cascade,
    comment text not null,
    resolved_at timestamp with time zone,
    resolved_by bigint
        constraint project_visibility_changes_resolved_by_fkey
            references users
            on delete cascade,
    visibility integer not null
);

alter table project_visibility_changes owner to hangar;

create table project_version_visibility_changes
(
    id bigserial not null
        constraint project_version_visibility_changes_pkey
            primary key,
    created_at timestamp with time zone default now() not null,
    created_by bigint not null
        constraint project_version_visibility_changes_created_by_fkey
            references users
            on delete cascade,
    version_id bigint not null
        constraint project_version_visibility_changes_version_id_fkey
            references project_versions
            on delete cascade,
    comment text not null,
    resolved_at timestamp with time zone,
    resolved_by bigint
        constraint project_version_visibility_changes_resolved_by_fkey
            references users
            on delete cascade,
    visibility integer not null
);

alter table project_version_visibility_changes owner to hangar;

create table project_version_tags
(
    id bigserial not null
        constraint project_version_tags_pkey
            primary key,
    version_id bigint not null
        constraint project_version_tags_version_id_fkey
            references project_versions
            on delete cascade,
    name varchar(255) not null,
    data varchar(255),
    color integer not null
);

alter table project_version_tags owner to hangar;

create index projects_versions_tags_version_id
    on project_version_tags (version_id);

create index project_version_tags_name_data_idx
    on project_version_tags (name, data);

create table roles
(
    id bigint not null
        constraint roles_pkey
            primary key,
    name varchar(255) not null,
    category role_category not null,
    title varchar(255) not null,
    color varchar(255) not null,
    is_assignable boolean not null,
    rank integer,
    permission bit(64) default '0'::bit(64) not null
);

alter table roles owner to hangar;

create unique index role_name_idx
    on roles (name);

create table user_global_roles
(
    user_id bigint not null
        constraint user_global_roles_user_id_fkey
            references users
            on delete cascade,
    role_id bigint not null
        constraint user_global_roles_role_id_fkey
            references roles
            on delete cascade,
    constraint user_global_roles_pkey
        primary key (user_id, role_id)
);

alter table user_global_roles owner to hangar;

create table api_keys
(
    id bigserial not null
        constraint api_keys_pkey
            primary key,
    created_at timestamp with time zone not null,
    name varchar(255) not null,
    owner_id bigint not null
        constraint api_keys_owner_id_fkey
            references users
            on delete cascade,
    token_identifier varchar(255) not null
        constraint api_keys_token_identifier_key
            unique,
    token text not null,
    raw_key_permissions bit(64) not null,
    constraint api_keys_owner_id_name_key
        unique (owner_id, name)
);

alter table api_keys owner to hangar;

create table api_sessions
(
    id bigserial not null
        constraint api_sessions_pkey
            primary key,
    created_at timestamp with time zone not null,
    token varchar(255) not null,
    key_id bigint
        constraint api_sessions_key_id_fkey
            references api_keys
            on delete cascade,
    user_id bigint
        constraint api_sessions_user_id_fkey
            references users
            on delete cascade,
    expires timestamp with time zone not null
);

alter table api_sessions owner to hangar;

create table logged_actions_project
(
    id bigserial not null
        constraint logged_actions_project_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint
        constraint logged_actions_project_user_id_fkey
            references users
            on delete set null,
    address inet not null,
    action logged_action_type not null,
    project_id bigint
        constraint logged_actions_project_project_id_fkey
            references projects
            on delete set null,
    new_state text not null,
    old_state text not null
);

alter table logged_actions_project owner to hangar;

create table logged_actions_version
(
    id bigserial not null
        constraint logged_actions_version_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint
        constraint logged_actions_version_user_id_fkey
            references users
            on delete set null,
    address inet not null,
    action logged_action_type not null,
    project_id bigint
        constraint logged_actions_version_project_id_fkey
            references projects
            on delete set null,
    version_id bigint
        constraint logged_actions_version_version_id_fkey
            references project_versions
            on delete set null,
    new_state text not null,
    old_state text not null
);

alter table logged_actions_version owner to hangar;

create table logged_actions_page
(
    id bigserial not null
        constraint logged_actions_page_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint
        constraint logged_actions_page_user_id_fkey
            references users
            on delete set null,
    address inet not null,
    action logged_action_type not null,
    project_id bigint
        constraint logged_actions_page_project_id_fkey
            references projects
            on delete set null,
    page_id bigint
        constraint logged_actions_page_page_id_fkey
            references project_pages
            on delete set null,
    new_state text not null,
    old_state text not null
);

alter table logged_actions_page owner to hangar;

create table logged_actions_user
(
    id bigserial not null
        constraint logged_actions_user_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint
        constraint logged_actions_user_user_id_fkey
            references users
            on delete set null,
    address inet not null,
    action logged_action_type not null,
    subject_id bigint
        constraint logged_actions_user_subject_id_fkey
            references users
            on delete set null,
    new_state text not null,
    old_state text not null
);

alter table logged_actions_user owner to hangar;

create table logged_actions_organization
(
    id bigserial not null
        constraint logged_actions_organization_pkey
            primary key,
    created_at timestamp with time zone not null,
    user_id bigint
        constraint logged_actions_organization_user_id_fkey
            references users
            on delete set null,
    address inet not null,
    action logged_action_type not null,
    organization_id bigint
        constraint logged_actions_organization_organization_id_fkey
            references organizations
            on delete set null,
    new_state text not null,
    old_state text not null
);

alter table logged_actions_organization owner to hangar;

create table project_versions_downloads_individual
(
    id bigserial not null
        constraint project_versions_downloads_individual_pkey
            primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null
        constraint project_versions_downloads_individual_project_id_fkey
            references projects
            on delete cascade,
    version_id bigint not null
        constraint project_versions_downloads_individual_version_id_fkey
            references project_versions
            on delete cascade,
    address inet not null,
    cookie varchar(36) not null,
    user_id bigint
        constraint project_versions_downloads_individual_user_id_fkey
            references users
            on delete set null,
    processed integer default 0 not null
);

alter table project_versions_downloads_individual owner to hangar;

create table project_versions_downloads
(
    day date not null,
    project_id bigint not null
        constraint project_versions_downloads_project_id_fkey
            references projects
            on delete cascade,
    version_id bigint not null
        constraint project_versions_downloads_version_id_fkey
            references project_versions
            on delete cascade,
    downloads integer not null,
    constraint project_versions_downloads_pkey
        primary key (day, version_id)
);

alter table project_versions_downloads owner to hangar;

create index project_versions_downloads_project_id_version_id_idx
    on project_versions_downloads (project_id, version_id);

create table project_views_individual
(
    id bigserial not null
        constraint project_views_individual_pkey
            primary key,
    created_at timestamp with time zone not null,
    project_id bigint not null
        constraint project_views_individual_project_id_fkey
            references projects
            on delete cascade,
    address inet not null,
    cookie varchar(36) not null,
    user_id bigint
        constraint project_views_individual_user_id_fkey
            references users
            on delete set null,
    processed integer default 0 not null
);

alter table project_views_individual owner to hangar;

create table project_views
(
    day date not null,
    project_id bigint not null
        constraint project_views_project_id_fkey
            references projects
            on delete cascade,
    views integer not null,
    constraint project_views_pkey
        primary key (project_id, day)
);

alter table project_views owner to hangar;

create table jobs
(
    id bigserial not null
        constraint jobs_pkey
            primary key,
    created_at timestamp with time zone not null,
    last_updated timestamp with time zone,
    retry_at timestamp with time zone,
    last_error text,
    last_error_descriptor text,
    state job_state not null,
    job_type text not null,
    job_properties hstore not null
);

alter table jobs owner to hangar;

create view project_members_all(id, user_id) as
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

alter table project_members_all owner to hangar;

create materialized view home_projects as
WITH tags AS (
    SELECT sq.project_id,
           sq.version_string,
           sq.tag_name,
           sq.tag_version,
           sq.tag_color
    FROM (SELECT pv.project_id,
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
                                    WHEN pvti.name::text = 'Sponge'::text THEN "substring"(pvti.data::text,
                                                                                           '^\[?(\d+)\.\d+(?:\.\d+)?(?:-SNAPSHOT)?(?:-[a-z0-9]{7,9})?(?:,(?:\d+\.\d+(?:\.\d+)?)?\))?$'::text)
                                    WHEN pvti.name::text = 'SpongeForge'::text THEN "substring"(pvti.data::text,
                                                                                                '^\d+\.\d+\.\d+-\d+-(\d+)\.\d+\.\d+(?:(?:-BETA-\d+)|(?:-RC\d+))?$'::text)
                                    WHEN pvti.name::text = 'SpongeVanilla'::text THEN "substring"(pvti.data::text,
                                                                                                  '^\d+\.\d+\.\d+-(\d+)\.\d+\.\d+(?:(?:-BETA-\d+)|(?:-RC\d+))?$'::text)
                                    WHEN pvti.name::text = 'Forge'::text
                                        THEN "substring"(pvti.data::text, '^\d+\.(\d+)\.\d+(?:\.\d+)?$'::text)
                                    WHEN pvti.name::text = 'Lantern'::text THEN NULL::text
                                    WHEN pvti.name::text = 'Paper'::text THEN pvti.data::text
                                    WHEN pvti.name::text = 'Waterfall'::text THEN pvti.data::text
                                    WHEN pvti.name::text = 'Velocity'::text THEN pvti.data::text
                                    ELSE NULL::text
                                    END AS platform_version,
                                pvti.color
                         FROM project_version_tags pvti
                         WHERE (pvti.name::text = ANY
                                (ARRAY ['Sponge'::character varying, 'SpongeForge'::character varying, 'SpongeVanilla'::character varying, 'Forge'::character varying, 'Lantern'::character varying, 'Paper'::character varying, 'Waterfall'::character varying, 'Velocity'::character varying]::text[]))
                           AND pvti.data IS NOT NULL) pvt ON pv.id = pvt.version_id
          WHERE pv.visibility = 1
            AND (pvt.name::text = ANY
                 (ARRAY ['Sponge'::character varying, 'SpongeForge'::character varying, 'SpongeVanilla'::character varying, 'Forge'::character varying, 'Lantern'::character varying, 'Paper'::character varying, 'Waterfall'::character varying, 'Velocity'::character varying]::text[]))
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
       p.plugin_id,
       p.created_at,
       max(lv.created_at)                        AS last_updated,
       to_jsonb(ARRAY(SELECT jsonb_build_object('version_string', tags.version_string, 'tag_name', tags.tag_name,
                                                'tag_version', tags.tag_version, 'tag_color',
                                                tags.tag_color) AS jsonb_build_object
                      FROM tags
                      WHERE tags.project_id = p.id
                      LIMIT 5))                  AS promoted_versions,
       ((setweight((to_tsvector('english'::regconfig, p.name::text) ||
                    to_tsvector('english'::regconfig, regexp_replace(p.name::text, '([a-z])([A-Z]+)'::text,
                                                                     '\1_\2'::text, 'g'::text))) ||
                   to_tsvector('english'::regconfig, p.plugin_id::text), 'A'::"char") ||
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
                           count(*) AS stars
                    FROM projects p_1
                             LEFT JOIN project_stars ps_1 ON p_1.id = ps_1.project_id
                    GROUP BY p_1.id) ps ON p.id = ps.id
         LEFT JOIN (SELECT p_1.id,
                           count(*) AS watchers
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

alter materialized view home_projects owner to hangar;

create view global_trust(user_id, permission) as
SELECT gr.user_id,
       COALESCE(bit_or(r.permission), '0'::bit(64)) AS permission
FROM user_global_roles gr
         JOIN roles r ON gr.role_id = r.id
GROUP BY gr.user_id;

alter table global_trust owner to hangar;

create view project_trust(project_id, user_id, permission) as
SELECT pm.project_id,
       pm.user_id,
       COALESCE(bit_or(r.permission), '0'::bit(64)) AS permission
FROM project_members pm
         JOIN user_project_roles rp ON pm.project_id = rp.project_id AND pm.user_id = rp.user_id AND rp.is_accepted
         JOIN roles r ON rp.role_type::text = r.name::text
GROUP BY pm.project_id, pm.user_id;

alter table project_trust owner to hangar;

create view organization_trust(organization_id, user_id, permission) as
SELECT om.organization_id,
       om.user_id,
       COALESCE(bit_or(r.permission), '0'::bit(64)) AS permission
FROM organization_members om
         JOIN user_organization_roles ro
              ON om.organization_id = ro.organization_id AND om.user_id = ro.user_id AND ro.is_accepted
         JOIN roles r ON ro.role_type::text = r.name::text
GROUP BY om.organization_id, om.user_id;

alter table organization_trust owner to hangar;

create view v_logged_actions(id, created_at, user_id, user_name, address, action, context_type, new_state, old_state, p_id, p_plugin_id, p_slug, p_owner_name, pv_id, pv_version_string, pp_id, pp_name, pp_slug, s_id, s_name) as
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
       p.plugin_id                  AS p_plugin_id,
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
       p.plugin_id             AS p_plugin_id,
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
       p.plugin_id             AS p_plugin_id,
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
       NULL::character varying AS p_plugin_id,
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
       NULL::character varying AS p_plugin_id,
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

alter table v_logged_actions owner to hangar;

create function delete_old_project_version_download_warnings() returns trigger
    language plpgsql
as $$
BEGIN
    DELETE FROM project_version_download_warnings WHERE created_at < current_date - interval '30' day;
    RETURN NEW;
END
$$;

alter function delete_old_project_version_download_warnings() owner to hangar;

create trigger clean_old_project_version_download_warnings
    after insert
    on project_version_download_warnings
execute procedure delete_old_project_version_download_warnings();

create function delete_old_project_version_unsafe_downloads() returns trigger
    language plpgsql
as $$
BEGIN
    DELETE FROM project_version_unsafe_downloads WHERE created_at < current_date - interval '30' day;
    RETURN NEW;
END
$$;

alter function delete_old_project_version_unsafe_downloads() owner to hangar;

create trigger clean_old_project_version_unsafe_downloads
    after insert
    on project_version_unsafe_downloads
execute procedure delete_old_project_version_unsafe_downloads();

create function update_project_name_trigger() returns trigger
    language plpgsql
as $$
BEGIN
    UPDATE projects p SET name = u.name FROM users u WHERE p.id = new.id AND u.id = new.owner_id;
END;
$$;

alter function update_project_name_trigger() owner to hangar;

create trigger project_owner_name_updater
    after update
        of owner_id
    on projects
    for each row
    when (old.owner_id <> new.owner_id)
execute procedure update_project_name_trigger();

create function logged_action_type_from_int(id integer) returns logged_action_type
    immutable
    strict
    language plpgsql
as $$
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

alter function logged_action_type_from_int(integer) owner to hangar;

create function websearch_to_tsquery_postfix(dictionary regconfig, query text) returns tsquery
    immutable
    strict
    language plpgsql
as $$
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

alter function websearch_to_tsquery_postfix(regconfig, text) owner to hangar;
