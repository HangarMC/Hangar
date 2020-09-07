package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.model.ProjectVersionsDownloadsIndividualTable;
import io.papermc.hangar.db.model.ProjectVersionsDownloadsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
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
              "     (:userId IS NOT NULL AND :userId = pvdi.user_id)")
    Optional<String> getIndividualDownloadCookie(Long userId, InetAddress address);

    @Timestamped
    @SqlUpdate("INSERT INTO project_versions_downloads_individual (created_at, project_id, version_id, address, cookie, user_id) VALUES (:now, :projectId, :versionId, :address, :cookie, :userId)")
    void addVersionDownload(long projectId, long versionId, InetAddress address, String cookie, Long userId);

    @SqlQuery("SELECT pvi.cookie " +
              "     FROM project_views_individual pvi" +
              "     WHERE pvi.address = :address OR " +
              "     (:userId IS NOT NULL AND :userId = pvi.user_id)")
    Optional<String> getIndividualViewCookie(Long userId, InetAddress address);

    @Timestamped
    @SqlUpdate("INSERT INTO project_views_individual (created_at, project_id, address, cookie, user_id) VALUES (:now, :projectId, :address, :cookie, :userId)")
    void addProjectView(long projectId, InetAddress address, String cookie, Long userId);
}
