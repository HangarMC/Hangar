package io.papermc.hangar.components.health;

import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import java.util.List;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(UnhealthyProject.class)
@RegisterConstructorMapper(HealthReportTable.class)
interface HealthDAO {

    // TODO remove view, what about last_updated?
    @SqlQuery("""
         SELECT hp.owner_name "owner",
                  hp.slug,
                  hp.last_updated,
                  hp.visibility
           FROM home_projects hp
           WHERE hp.last_updated < (now() - interval <age>)
           ORDER BY hp.last_updated DESC
        """)
    List<UnhealthyProject> getStaleProjects(@Define("age") String staleAgeSeconds);

    // TODO remove view, what about last_updated?
    @SqlQuery("""
         SELECT hp.owner_name "owner",
                  hp.slug,
                  hp.last_updated,
                  hp.visibility
           FROM home_projects hp
           WHERE hp.visibility != 0
           ORDER BY hp.created_at DESC
        """)
    List<UnhealthyProject> getNonPublicProjects();

    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @RegisterConstructorMapper(MissingFileCheck.class)
    @SqlQuery("""
        SELECT pv.version_string,
               p.owner_name                                        AS owner,
               p.slug,
               array_agg(pvd.file_name)                            AS filenames,
               array_agg(DISTINCT platform ORDER BY platform DESC) AS platforms
        FROM project_versions pv
                 JOIN projects p ON p.id = pv.project_id
                 LEFT JOIN project_version_downloads pvd ON pvd.version_id = pv.id
                 CROSS JOIN unnest(pvd.platforms) AS platform
        WHERE pvd.id IS NOT NULL
          AND pvd.file_name IS NOT NULL
        GROUP BY p.id, pv.id;""")
    List<MissingFileCheck> getVersionsForMissingFiles();

    @GetGeneratedKeys
    @SqlUpdate("""
        INSERT INTO health_reports (report, queued_by, queued_at, finished_at, status)
        VALUES (:report, :queuedBy, :queuedAt, :finishedAt, :status)
        """)
    HealthReportTable insertHealthReport(@BindBean HealthReportTable healthReport);

    @SqlUpdate("""
        UPDATE health_reports
        SET report      = :report,
            queued_by   = :queuedBy,
            queued_at   = :queuedAt,
            finished_at = :finishedAt,
            status      = :status
        WHERE id = :id
        """)
    void updateHealthReport(@BindBean HealthReportTable healthReport);

    @SqlQuery("""
        SELECT id, report, queued_by, queued_at, finished_at, status
        FROM health_reports
        ORDER BY queued_at DESC
        LIMIT 1
        """)
    HealthReportTable getHealthReport();
}
