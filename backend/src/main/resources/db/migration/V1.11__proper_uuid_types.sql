ALTER TABLE project_versions_downloads_individual ALTER COLUMN cookie TYPE UUID USING cookie::UUID;
ALTER TABLE project_views_individual ALTER COLUMN cookie TYPE UUID USING cookie::UUID;
ALTER TABLE api_keys ALTER COLUMN token_identifier TYPE UUID USING token_identifier::UUID;

CREATE INDEX idx_api_keys_owner_tokenid ON api_keys (owner_id, token_identifier);
