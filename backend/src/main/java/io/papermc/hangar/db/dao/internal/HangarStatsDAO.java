package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.admin.DayStats;
import java.time.LocalDate;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
public interface HangarStatsDAO {

    @SqlQuery
    @UseClasspathSqlLocator
    @RegisterConstructorMapper(DayStats.class)
    List<DayStats> getStats(LocalDate startDate, LocalDate endDate);

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
