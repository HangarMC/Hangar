CREATE OR REPLACE FUNCTION delete_old_project_version_download_warnings() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM project_version_download_warnings WHERE created_at < current_date - interval '30' day;
    RETURN NEW;
END
$$;

CREATE OR REPLACE TRIGGER clean_old_project_version_download_warnings
    AFTER INSERT
    ON project_version_download_warnings
EXECUTE PROCEDURE delete_old_project_version_download_warnings();

CREATE OR REPLACE FUNCTION delete_old_project_version_unsafe_downloads() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM project_version_unsafe_downloads WHERE created_at < current_date - interval '30' day;
    RETURN NEW;
END
$$;

CREATE OR REPLACE TRIGGER clean_old_project_version_unsafe_downloads
    AFTER INSERT
    ON project_version_unsafe_downloads
EXECUTE PROCEDURE delete_old_project_version_unsafe_downloads();

CREATE OR REPLACE FUNCTION update_project_name_trigger() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE projects p SET name = u.name FROM users u WHERE p.id = new.id AND u.id = new.owner_id;
END;
$$;

CREATE OR REPLACE TRIGGER project_owner_name_updater
    AFTER UPDATE
        OF owner_id
    ON projects
    FOR EACH ROW
    WHEN  (old.owner_id <> new.owner_id)
EXECUTE PROCEDURE update_project_name_trigger();

CREATE OR REPLACE FUNCTION websearch_to_tsquery_postfix(dictionary regconfig, query text) RETURNS tsquery
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
