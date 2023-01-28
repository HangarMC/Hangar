package io.papermc.hangar.db.dao.internal.table.stats;

import io.papermc.hangar.model.db.stats.ProjectVersionDownloadIndividualTable;
import java.net.InetAddress;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionDownloadIndividualTable.class)
public interface ProjectVersionDownloadStatsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO project_versions_downloads_individual (created_at, project_id, version_id, address, cookie, user_id, platform) VALUES (:now, :projectId, :versionId, :address, :cookie, :userId, :platform)")
    void insert(@BindBean ProjectVersionDownloadIndividualTable projectVersionDownloadIndividualTable);

    @SqlQuery("SELECT * FROM project_versions_downloads_individual WHERE address = :address OR (:userId IS NOT NULL AND user_id = :userId) LIMIT 1")
    Optional<ProjectVersionDownloadIndividualTable> getIndividualDownload(Long userId, InetAddress address);
}
