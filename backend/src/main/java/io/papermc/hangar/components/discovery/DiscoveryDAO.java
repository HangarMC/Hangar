package io.papermc.hangar.components.discovery;

import io.papermc.hangar.model.api.project.ProjectCompact;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
public interface DiscoveryDAO {

    /**
     * Picks {@code perBucket} projects from each third of the catalogue by download count, so the result mixes
     * well known projects with ones nobody has seen. Within a bucket the pick is the window of seeds starting at
     * {@code cursor} and wrapping past 1, which walks the whole eligible pool as the cursor advances day by day.
     *
     * @param maxAgeDays how long since the last release before a project stops being surfaced, or 0 for no limit
     */
    @SqlQuery("""
        WITH eligible AS (
            SELECT p.id,
                   p.discovery_seed,
                   ntile(3) OVER (ORDER BY hp.downloads) AS bucket
            FROM projects p
                JOIN home_projects hp ON hp.id = p.id
            WHERE p.visibility = 0
              AND length(trim(coalesce(p.description, ''))) > 0
              AND (:maxAgeDays <= 0 OR hp.last_updated > now() - make_interval(days => :maxAgeDays))
              AND EXISTS (SELECT 1 FROM project_versions pv WHERE pv.project_id = p.id AND pv.visibility = 0)
              AND NOT EXISTS (SELECT 1 FROM discovery_exclusions de WHERE de.project_id = p.id)
        ),
        picked AS (
            SELECT id,
                   bucket,
                   row_number() OVER (
                       PARTITION BY bucket
                       ORDER BY CASE
                                    WHEN discovery_seed >= :cursor THEN discovery_seed - :cursor
                                    ELSE discovery_seed + 1 - :cursor
                                END
                   ) AS rn
            FROM eligible
        )
        SELECT hp.id,
               hp.created_at,
               hp.name,
               hp.owner_name AS owner,
               hp.slug,
               hp.description,
               hp.views,
               hp.downloads,
               hp.recent_views,
               hp.recent_downloads,
               hp.stars,
               hp.watchers,
               hp.category,
               hp.last_updated,
               hp.visibility,
               hp.avatar,
               hp.avatar_fallback
        FROM picked
            JOIN home_projects hp ON hp.id = picked.id
        WHERE picked.rn <= :perBucket
        ORDER BY picked.rn, picked.bucket
        """)
    @RegisterConstructorMapper(ProjectCompact.class)
    List<ProjectCompact> discover(double cursor, int perBucket, int maxAgeDays);

    @SqlUpdate("INSERT INTO discovery_exclusions (project_id, created_by) VALUES (:projectId, :userId) ON CONFLICT DO NOTHING")
    void exclude(long projectId, long userId);

    @SqlUpdate("DELETE FROM discovery_exclusions WHERE project_id = :projectId")
    void include(long projectId);

    @SqlQuery("""
        SELECT de.project_id,
               p.name,
               p.owner_name AS owner,
               p.slug,
               de.created_at,
               u.name       AS excluded_by
        FROM discovery_exclusions de
            JOIN projects p ON p.id = de.project_id
            LEFT JOIN users u ON u.id = de.created_by
        ORDER BY de.created_at DESC
        """)
    @RegisterConstructorMapper(ExcludedProject.class)
    List<ExcludedProject> excluded();
}
