package io.papermc.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlCall;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import io.papermc.hangar.db.model.ProjectVersionDownloadWarningsTable;

@Repository
@RegisterBeanMapper(ProjectVersionDownloadWarningsTable.class)
public interface ProjectVersionDownloadWarningDao {

    @SqlQuery("SELECT * FROM project_version_download_warnings " +
              "WHERE token == :token" +
              "    AND version_id == :versionId" +
              "    AND address == :address" +
              "    AND is_confirmed IS true")
    ProjectVersionDownloadWarningsTable find(String token, long versionId, String address);

    @SqlUpdate("DELETE FROM project_version_download_warnings WHERE id == :id")
    void delete(@BindBean ProjectVersionDownloadWarningsTable warning);
}
