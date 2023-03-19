ALTER TABLE projects
    ADD links jsonb;

CREATE OR REPLACE FUNCTION get_link_json(id bigint)
    RETURNS json
AS
$$
DECLARE
    result json;
BEGIN
    WITH project AS (SELECT * FROM projects p WHERE p.id = $1)
    SELECT json_build_array(json_build_object('id', 0, 'type', 'top', 'title', 'top', 'links',
                                              json_build_array(row_to_json(homepage), row_to_json(issues),
                                                               row_to_json(source),
                                                               row_to_json(support), row_to_json(wiki))))
    INTO result
    FROM (SELECT 0 AS id, 'Homepage' AS name, homepage AS url FROM project) homepage,
         (SELECT 1 AS id, 'Issues' AS name, issues AS url FROM project) issues,
         (SELECT 2 AS id, 'Source' AS name, source AS url FROM project) source,
         (SELECT 3 AS id, 'Support' AS name, support AS url FROM project) support,
         (SELECT 4 AS id, 'Wiki' AS name, wiki AS url FROM project) wiki;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

UPDATE projects p
SET links = get_link_json(p.id);

DROP FUNCTION get_link_json(bigint);

ALTER TABLE projects
    DROP COLUMN homepage;
ALTER TABLE projects
    DROP COLUMN issues;
ALTER TABLE projects
    DROP COLUMN source;
ALTER TABLE projects
    DROP COLUMN support;
ALTER TABLE projects
    DROP COLUMN wiki;
