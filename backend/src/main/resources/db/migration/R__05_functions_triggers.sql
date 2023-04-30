CREATE OR REPLACE FUNCTION update_project_name_trigger() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE projects p SET owner_name = u.name FROM users u WHERE p.id = new.id AND u.id = new.owner_id;
    RETURN NULL;
END;
$$;

CREATE OR REPLACE TRIGGER project_owner_name_updater
    AFTER UPDATE
        OF owner_id
    ON projects
    FOR EACH ROW
    WHEN (old.owner_id <> new.owner_id)
EXECUTE PROCEDURE update_project_name_trigger();

CREATE OR REPLACE FUNCTION websearch_to_tsquery_postfix(dictionary regconfig, query text) RETURNS tsquery
    IMMUTABLE
    STRICT
    LANGUAGE plpgsql
AS
$$
DECLARE
    arr  text[]  := regexp_split_to_array(query, '\s+');
    last text    := websearch_to_tsquery('simple', arr[array_length(arr, 1)])::text;
    init tsquery := websearch_to_tsquery(dictionary, regexp_replace(query, '\S+$', ''));
BEGIN
    IF last = '' THEN
        BEGIN
            RETURN init && $2::tsquery;
        EXCEPTION
            WHEN SYNTAX_ERROR THEN
                RETURN init && websearch_to_tsquery('');
        END;
    END IF;

    RETURN init && (websearch_to_tsquery(dictionary, last) || to_tsquery('simple', last || ':*'));
END;
$$;
