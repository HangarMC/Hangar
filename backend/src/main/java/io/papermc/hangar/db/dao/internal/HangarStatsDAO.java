package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.admin.DayStats;
import io.papermc.hangar.model.internal.admin.PlatformDownloads;
import io.papermc.hangar.model.internal.admin.StatsTotals;
import io.papermc.hangar.model.internal.admin.TopProject;
import java.time.LocalDate;
import java.util.List;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

@JdbiRepository
public interface HangarStatsDAO {

    @SqlQuery
    @UseClasspathSqlLocator
    @RegisterConstructorMapper(DayStats.class)
    List<DayStats> getStats(LocalDate startDate, LocalDate endDate);

    @SqlQuery("""
        SELECT (SELECT count(*) FROM users)                                                          AS users,
               (SELECT count(*) FROM projects WHERE visibility != 4)                                 AS projects,
               (SELECT count(*) FROM project_versions WHERE visibility != 4)                         AS versions,
               (SELECT coalesce(sum(downloads), 0) FROM project_versions_downloads)                  AS downloads,
               (SELECT coalesce(sum(views), 0) FROM project_views)                                   AS views,
               (SELECT count(*) FROM project_flags WHERE NOT resolved)                               AS open_flags,
               (SELECT count(*)
                FROM project_versions pv
                    JOIN projects p ON pv.project_id = p.id
                WHERE pv.review_state = 0 AND p.visibility != 4 AND pv.visibility != 4)              AS pending_reviews
        """)
    @RegisterConstructorMapper(StatsTotals.class)
    StatsTotals getTotals();

    @SqlQuery("""
        SELECT pvd.platform, sum(pvd.downloads) AS downloads
        FROM project_versions_downloads pvd
        WHERE pvd.day BETWEEN :startDate AND :endDate AND pvd.platform >= 0
        GROUP BY pvd.platform
        ORDER BY downloads DESC
        """)
    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @RegisterConstructorMapper(PlatformDownloads.class)
    List<PlatformDownloads> getPlatformDownloads(LocalDate startDate, LocalDate endDate);

    @SqlQuery("""
        SELECT p.owner_name                    AS owner,
               p.slug,
               coalesce(dl.downloads, 0)       AS downloads,
               coalesce(v.views, 0)            AS views
        FROM projects p
            LEFT JOIN (SELECT pvd.project_id, sum(pvd.downloads) AS downloads
                       FROM project_versions_downloads pvd
                       WHERE pvd.day BETWEEN :startDate AND :endDate
                       GROUP BY pvd.project_id) dl ON dl.project_id = p.id
            LEFT JOIN (SELECT pv.project_id, sum(pv.views) AS views
                       FROM project_views pv
                       WHERE pv.day BETWEEN :startDate AND :endDate
                       GROUP BY pv.project_id) v ON v.project_id = p.id
        WHERE p.visibility != 4 AND (dl.downloads > 0 OR v.views > 0)
        ORDER BY downloads DESC, views DESC
        LIMIT :limit
        """)
    @RegisterConstructorMapper(TopProject.class)
    List<TopProject> getTopProjects(LocalDate startDate, LocalDate endDate, int limit);

    @SqlUpdate("""
        UPDATE <table> AS pvdi
        SET user_id = pvdi2.user_id
        FROM (
          SELECT DISTINCT cookie, user_id
          FROM project_versions_downloads_individual
          WHERE user_id IS NOT NULL
        ) AS pvdi2
        WHERE pvdi.cookie = pvdi2.cookie AND pvdi.user_id IS NULL AND pvdi.processed = 0
                                """)
    void fillStatsUserIdsFromOthers(@Define String table);

    @UseStringTemplateEngine
    @SqlUpdate("""
        WITH d AS (
             UPDATE <individualTable> SET processed = processed + 1
             WHERE user_id IS <if(withUserId)>NOT<endif> NULL
             RETURNING created_at, project_id, <if(downloads)>version_id, platform,<endif> <if(withUserId)>user_id<else>address<endif>, processed
         )
         INSERT
             INTO <dayTable> AS pvd (day, project_id, <if(downloads)>version_id, platform,<endif> <statColumn>)
         SELECT sq.day,
             sq.project_id,
             <if(downloads)>sq.version_id, sq.platform,<endif>
             <if(withUserId)>count(DISTINCT sq.user_id)<else>count(distinct network(case
                                               when family(sq.address::inet) = 6 then set_masklen(sq.address, 48)
                                               when family(sq.address::inet) = 4 then set_masklen(sq.address, 32)
                                               else null end
                        )::inet)<endif> FILTER (WHERE sq.processed \\<@ ARRAY[1])
         FROM (SELECT date_trunc('DAY', d.created_at)::date AS day,
                     d.project_id,
                     <if(downloads)>d.version_id, d.platform,<endif>
                     <if(withUserId)>user_id<else>address<endif>,
                     array_agg(d.processed) AS processed
                 FROM d
                 GROUP BY date_trunc('DAY', d.created_at), d.project_id, <if(downloads)>d.version_id, d.platform,<endif> <if(withUserId)>user_id<else>address<endif>) sq
             GROUP BY sq.day, <if(downloads)>sq.version_id, sq.platform,<endif> sq.project_id
         ON CONFLICT(day, <if(downloads)>version_id, platform<else>project_id<endif>) DO UPDATE SET <statColumn> = pvd.<statColumn> + excluded.<statColumn>
    """)
    void processStatsMain(@Define String individualTable, @Define String dayTable, @Define String statColumn, @Define boolean withUserId, @Define boolean downloads);

    @SqlUpdate("DELETE FROM <table> WHERE processed != 0 AND created_at < now() - '7 days'::INTERVAL")
    void deleteOldIndividual(@Define String table);
}
