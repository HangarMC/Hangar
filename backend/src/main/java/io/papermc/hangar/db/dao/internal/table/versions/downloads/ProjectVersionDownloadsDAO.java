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
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionPlatformDownloadTable.class)
@RegisterConstructorMapper(ProjectVersionDownloadTable.class)
public interface ProjectVersionDownloadsDAO {

    @SqlBatch("INSERT INTO project_version_platform_downloads (version_id, platform, download_id) VALUES (:versionId, :platform, :downloadId)")
    void insertPlatformDownloads(@BindBean Collection<ProjectVersionPlatformDownloadTable> projectVersionPlatformDownloadTables);

    @SqlUpdate("UPDATE project_version_downloads SET hash = :hash WHERE id = :id")
    void updateHash(long id, String hash);

    @GetGeneratedKeys
    @SqlBatch("INSERT INTO project_version_downloads (version_id, file_size, hash, file_name, external_url) VALUES (:versionId, :fileSize, :hash, :fileName, :externalUrl)")
    List<ProjectVersionDownloadTable> insertDownloads(@BindBean Collection<ProjectVersionDownloadTable> projectVersionDownloadTables);

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId")
    List<ProjectVersionDownloadTable> getDownloads(long versionId);

    @SqlQuery("SELECT * FROM project_version_downloads")
    List<ProjectVersionDownloadTable> getDownloads();

    @SqlQuery("SELECT * FROM project_version_platform_downloads WHERE version_id = :versionId")
    List<ProjectVersionPlatformDownloadTable> getPlatformDownloads(long versionId);

    @SqlQuery("SELECT * FROM project_version_platform_downloads WHERE version_id = :versionId AND download_id = :downloadId")
    List<ProjectVersionPlatformDownloadTable> getPlatformDownloads(long versionId, long downloadId);

    @SqlQuery("SELECT * FROM project_version_platform_downloads WHERE version_id = :versionId AND platform = :platform")
    ProjectVersionPlatformDownloadTable getPlatformDownload(long versionId, @EnumByOrdinal Platform platform);

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId AND id = :downloadId")
    ProjectVersionDownloadTable getDownload(long versionId, long downloadId);
}
