-- Some projects created before link validation existed have sections with "links": null or links without a url,
-- while the API documents both as required
--
-- projects.links is a JSON array of sections, each section holding its own array of links:
--   [{"id": 0, "type": "top", "title": "top", "links": [{"id": 0, "name": "Source", "url": "https://..."}]}]
-- This helper takes that array and returns a cleaned copy:
--   - every section gets a "links" array ("links": null or a missing "links" becomes [])
--   - links without a "url" are dropped
CREATE OR REPLACE FUNCTION Fix_link_sections(sections JSONB
) RETURNS JSONB AS $$ DECLARE
    section JSONB;      -- the section currently being processed
    link JSONB;         -- the link currently being processed
    valid_links JSONB;  -- the links of the current section that have a url
    result JSONB := '[]'; -- the cleaned sections, filled one by one
BEGIN
    -- the whole column is NULL or not an array: replace it with an empty list
    IF Jsonb_typeof(sections) IS DISTINCT FROM 'array' THEN
        RETURN '[]';
    END IF;
    -- Jsonb_array_elements turns a JSON array into one row per element, so we can loop over it
    FOR section IN SELECT *
                   FROM   Jsonb_array_elements(sections) LOOP
            valid_links := '[]';
            -- only look at the links if they really are an array; null or missing simply stays []
            IF Jsonb_typeof(section -> 'links') = 'array' THEN
                FOR link IN SELECT *
                            FROM   Jsonb_array_elements(section -> 'links') LOOP
                        -- ->> reads a field as text; it is NULL if the field is missing or null
                        IF link ->> 'url' IS NOT NULL THEN
                            -- || appends to a JSON array
                            valid_links := valid_links || Jsonb_build_array(link);
                        END IF;
                    END LOOP;
            END IF;

            -- keep the section as it is (id, type, title), but replace its "links" with the cleaned list
            result := result || Jsonb_build_array(Jsonb_set(section, '{links}', valid_links));
        END LOOP;
    RETURN result;
END; $$ LANGUAGE plpgsql;

-- only touch projects whose links actually change, so correct projects stay untouched
-- (IS DISTINCT FROM, unlike <>, also treats NULL as a comparable value)
UPDATE projects
SET    links = Fix_link_sections(links)
WHERE  links IS DISTINCT FROM Fix_link_sections(links);

-- the function is only needed for this one-off fix
DROP FUNCTION fix_link_sections(JSONB);
