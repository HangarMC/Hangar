package io.papermc.hangar.db.dao.internal.table.versions.downloads;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionPlatformDownloadTable;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionPlatformDownloadTable.class)
@RegisterConstructorMapper(ProjectVersionDownloadTable.class)
public interface ProjectVersionDownloadsDAO {

    /*
                "       pv.file_name fi_name," +
            "       pv.file_size fi_size_bytes," +
            "       pv.hash fi_md5_hash," +
            "       pv.external_url," +
     */

    @SqlBatch("INSERT INTO project_version_platform_downloads (version_id, platform, download_id) VALUES (:versionId, :platform, :downloadId)")
    void insertPlatformDownloads(@BindBean Collection<ProjectVersionPlatformDownloadTable> projectVersionPlatformDownloadTables);

    @GetGeneratedKeys
    @SqlBatch("INSERT INTO project_version_downloads (version_id, file_size, external_url) VALUES (:versionId, :platform, :downloadId)")
    List<ProjectVersionDownloadTable> insertDownloads(@BindBean Collection<ProjectVersionDownloadTable> projectVersionDownloadTables);

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId")
    List<ProjectVersionDownloadTable> getDownloads(long versionId);

    @SqlQuery("SELECT * FROM project_version_platform_downloads WHERE version_id = :versionId")
    List<ProjectVersionPlatformDownloadTable> getPlatformDownloads(long versionId);

    @SqlQuery("SELECT * FROM project_version_platform_downloads WHERE version_id = :versionId AND platform = :platform")
    ProjectVersionPlatformDownloadTable getPlatformDownload(long versionId, @EnumByOrdinal Platform platform);

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId AND id = :downloadId")
    ProjectVersionDownloadTable getDownload(long versionId, long downloadId);
}
