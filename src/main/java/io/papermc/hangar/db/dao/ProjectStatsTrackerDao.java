package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.modelold.ProjectVersionsDownloadsIndividualTable;
import io.papermc.hangar.db.modelold.ProjectVersionsDownloadsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.util.Optional;

@Repository
@RegisterBeanMapper(ProjectVersionsDownloadsTable.class)
@RegisterBeanMapper(ProjectVersionsDownloadsIndividualTable.class)
public interface ProjectStatsTrackerDao {

    @SqlQuery("SELECT pvdi.cookie " +
              "     FROM project_versions_downloads_individual pvdi" +
              "     WHERE pvdi.address = :address OR " +
              "     (:userId IS NOT NULL AND :userId = pvdi.user_id) LIMIT 1")
    Optional<String> getIndividualDownloadCookie(Long userId, InetAddress address);

    @Timestamped
    @SqlUpdate("INSERT INTO project_versions_downloads_individual (created_at, project_id, version_id, address, cookie, user_id) VALUES (:now, :projectId, :versionId, :address, :cookie, :userId)")
    void addVersionDownload(long projectId, long versionId, InetAddress address, String cookie, Long userId);

    @SqlQuery("SELECT pvi.cookie " +
              "     FROM project_views_individual pvi" +
              "     WHERE pvi.address = :address OR " +
              "     (:userId IS NOT NULL AND :userId = pvi.user_id) LIMIT 1")
    Optional<String> getIndividualViewCookie(Long userId, InetAddress address);

    @Timestamped
    @SqlUpdate("INSERT INTO project_views_individual (created_at, project_id, address, cookie, user_id) VALUES (:now, :projectId, :address, :cookie, :userId)")
    void addProjectView(long projectId, InetAddress address, String cookie, Long userId);

    @SqlUpdate("UPDATE <table> pvdi SET user_id = (SELECT pvdi2.user_id FROM <table> pvdi2 WHERE pvdi2.user_id IS NOT NULL AND pvdi2.cookie = pvdi.cookie LIMIT 1) WHERE pvdi.user_id IS NULL AND pvdi.processed = 0")
    void fillStatsUserIdsFromOthers(@Define String table);

    @UseStringTemplateEngine
    @SqlUpdate("WITH d AS (" +
              "     UPDATE <individualTable> SET processed = processed + 1 " +
              "     WHERE user_id IS <if(withUserId)>NOT<endif> NULL" +
              "     RETURNING created_at, project_id, <if(includeVersionId)>version_id,<endif> <if(withUserId)>user_id<else>address<endif>, processed" +
              " )" +
              " INSERT " +
              "     INTO <dayTable> AS pvd (day, project_id, <if(includeVersionId)>version_id,<endif> <statColumn>)" +
              " SELECT sq.day," +
              "     sq.project_id," +
              "     <if(includeVersionId)>sq.version_id,<endif>" +
              "     count(DISTINCT sq.<if(withUserId)>user_id<else>address<endif>) FILTER (WHERE sq.processed \\<@ ARRAY[1])" +
              " FROM (SELECT date_trunc('DAY', d.created_at) AS day," +
              "             d.project_id," +
              "             <if(includeVersionId)>d.version_id,<endif>" +
              "             <if(withUserId)>user_id<else>address<endif>," +
              "             array_agg(d.processed) AS processed" +
              "         FROM d" +
              "         GROUP BY date_trunc('DAY', d.created_at), d.project_id, <if(includeVersionId)>d.version_id,<endif> <if(withUserId)>user_id<else>address<endif>) sq" +
              "     GROUP BY sq.day, <if(includeVersionId)>sq.version_id,<endif> sq.project_id" +
              " ON CONFLICT(day, <if(includeVersionId)>version_id<else>project_id<endif>) DO UPDATE SET <statColumn> = pvd.<if(includeVersionId)>version_id<else>project_id<endif> + excluded.<if(includeVersionId)>version_id<else>project_id<endif>")
    void processStatsMain(@Define String individualTable, @Define String dayTable, @Define String statColumn, @Define boolean withUserId, @Define boolean includeVersionId);

    @SqlUpdate("DELETE FROM <table> WHERE processed != 0 AND created_at < now() + '30 days'::INTERVAL")
    void deleteOldIndividual(@Define String table);
}
