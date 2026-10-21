-- Projects created before link validation can have sections with "links": null or links without a url
UPDATE projects p
SET    links = fixed.links
FROM   (SELECT id,
               coalesce((SELECT jsonb_agg(jsonb_set(s.section, '{links}', s.links) ORDER BY s.idx)
                         FROM   (SELECT e.section,
                                        e.idx,
                                        coalesce((SELECT jsonb_agg(l.link ORDER BY l.idx)
                                                  FROM   jsonb_array_elements(CASE WHEN jsonb_typeof(e.section -> 'links') = 'array' THEN e.section -> 'links' ELSE '[]' END) WITH ORDINALITY l(link, idx)
                                                  WHERE  jsonb_typeof(l.link) = 'object'
                                                    AND  l.link ->> 'url' IS NOT NULL), '[]') AS links
                                 FROM   jsonb_array_elements(CASE WHEN jsonb_typeof(projects.links) = 'array' THEN projects.links ELSE '[]' END) WITH ORDINALITY e(section, idx)
                                 WHERE  jsonb_typeof(e.section) = 'object') s
                         WHERE  s.links <> '[]'), '[]') AS links
        FROM   projects) fixed
WHERE  p.id = fixed.id
  AND  p.links IS DISTINCT FROM fixed.links;
