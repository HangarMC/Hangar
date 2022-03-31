package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.model.ProjectVersionDownloadWarningsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;

@Repository
@RegisterBeanMapper(ProjectVersionDownloadWarningsTable.class)
public interface ProjectVersionDownloadWarningDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_download_warnings (created_at, expiration, token, version_id, address, download_id) VALUES (:now, :expiration, :token, :versionId, :address, :downloadId)")
    ProjectVersionDownloadWarningsTable insert(@BindBean ProjectVersionDownloadWarningsTable projectVersionDownloadWarningsTable);

    @SqlQuery("SELECT * FROM project_version_download_warnings " +
              "WHERE token = :token" +
              "    AND version_id = :versionId" +
              "    AND address = :address" +
              "    AND is_confirmed IS TRUE")
    ProjectVersionDownloadWarningsTable findConfirmedWarning(String token, long versionId, InetAddress address);

    @SqlQuery("SELECT pvdw.* FROM project_version_download_warnings pvdw " +
              "WHERE pvdw.address = :address" +
              "    AND pvdw.token = :token" +
              "    AND pvdw.version_id = :versionId" +
              "    AND pvdw.is_confirmed IS FALSE" +
              "    AND pvdw.download_id IS NULL")
    ProjectVersionDownloadWarningsTable findUnconfirmedWarning(InetAddress address, String token, long versionId);

    @SqlUpdate("UPDATE project_version_download_warnings SET is_confirmed = :isConfirmed, download_id = :downloadId WHERE id = :id")
    void update(@BindBean ProjectVersionDownloadWarningsTable projectVersionDownloadWarningsTable);

    @SqlUpdate("DELETE FROM project_version_download_warnings WHERE id == :id")
    void delete(@BindBean ProjectVersionDownloadWarningsTable warning);

    @SqlUpdate("DELETE FROM project_version_download_warnings WHERE (address = :address OR expiration < now()) AND version_id = :versionId")
    void deleteInvalid(String address, long versionId);
}
