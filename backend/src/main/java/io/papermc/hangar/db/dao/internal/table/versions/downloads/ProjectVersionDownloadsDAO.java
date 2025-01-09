package io.papermc.hangar.db.dao.internal.table.versions.downloads;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadTable;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(ProjectVersionDownloadTable.class)
public interface ProjectVersionDownloadsDAO {

    @SqlUpdate("UPDATE project_version_downloads SET hash = :hash WHERE id = :id")
    void updateHash(long id, String hash);

    @SqlBatch("INSERT INTO project_version_downloads (version_id, file_size, hash, file_name, external_url, platforms, download_platform) VALUES (:versionId, :fileSize, :hash, :fileName, :externalUrl, :platforms, :downloadPlatform)")
    void insertDownloads(@BindBean Collection<ProjectVersionDownloadTable> projectVersionDownloadTables);

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId")
    List<ProjectVersionDownloadTable> getDownloads(long versionId);

    @SqlQuery("SELECT * FROM project_version_downloads")
    List<ProjectVersionDownloadTable> getDownloads();

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId AND :platform = any(platforms)")
    ProjectVersionDownloadTable getDownloadByPlatform(long versionId, @EnumByOrdinal Platform platform);

    @SqlQuery("SELECT * FROM project_version_downloads WHERE version_id = :versionId AND id = :downloadId")
    ProjectVersionDownloadTable getDownload(long versionId, long downloadId);
}
