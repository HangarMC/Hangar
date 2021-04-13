package io.papermc.hangar.db.dao.internal.table.versions.downloads;

import io.papermc.hangar.model.db.versions.downloads.ProjectVersionDownloadWarningTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;

@Repository
@RegisterConstructorMapper(ProjectVersionDownloadWarningTable.class)
public interface ProjectVersionDownloadWarningsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO project_version_download_warnings (created_at, expires_at, token, version_id, address, download_id) VALUES (:now, :expiresAt, :token, :versionId, :address, :downloadId)")
    void insert(@BindBean ProjectVersionDownloadWarningTable projectVersionDownloadWarningTable);

    @SqlQuery(" SELECT * " +
            "   FROM project_version_download_warnings " +
            "   WHERE token = :token AND" +
            "         address = :address AND" +
            "         version_id = :versionId")
    ProjectVersionDownloadWarningTable findWarning(String token, InetAddress address, long versionId);
}
