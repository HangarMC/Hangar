UPDATE project_pages
SET homepage = TRUE
WHERE id IN (SELECT page_id FROM project_home_pages);

UPDATE project_pages
SET homepage = FALSE
WHERE id NOT IN (SELECT page_id FROM project_home_pages);
