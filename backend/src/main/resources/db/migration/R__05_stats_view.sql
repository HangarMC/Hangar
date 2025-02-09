DROP MATERIALIZED VIEW IF EXISTS version_stats_view CASCADE;

CREATE MATERIALIZED VIEW version_stats_view AS
    SELECT pv.id,
           coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE pv.id = pvd.version_id),
                    0)                      AS totaldownloads,
           (SELECT json_agg(d.res)
            FROM (SELECT json_build_object('platform', pvd.platform, 'downloads', sum(pvd.downloads)) res
                  FROM project_versions_downloads pvd
                  WHERE pv.id = pvd.version_id
                  GROUP BY pvd.platform) d) AS platformdownloads
    FROM project_versions pv;

CREATE UNIQUE INDEX version_stats_view_id_idx ON version_stats_view (id);
