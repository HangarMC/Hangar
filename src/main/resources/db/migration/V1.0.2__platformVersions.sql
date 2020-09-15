CREATE TABLE platform_versions (
    id BIGSERIAL NOT NULL CONSTRAINT platform_versions_pkey PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    platform bigint NOT NULL,
    version varchar(255) NOT NULL
);

ALTER TABLE platform_versions OWNER TO hangar;