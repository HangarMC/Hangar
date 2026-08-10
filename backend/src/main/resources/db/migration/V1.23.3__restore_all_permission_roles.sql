-- V1.23.2 cleared bit 21 from every roles row, but these three are seeded from Permission.All, a blanket
-- mask over every bit rather than a list of grants. Losing the bit left Hangar_Admin short of All, which is
-- the check PopulationService uses to tell an already seeded roles table from an empty one.
UPDATE roles
SET permission = (permission::bigint | 2097152)::bit(64)
WHERE name IN ('Hangar_Admin', 'PaperMC_Core', 'PaperMC_CM');
