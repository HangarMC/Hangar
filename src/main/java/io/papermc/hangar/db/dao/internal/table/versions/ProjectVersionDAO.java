package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionTable.class)
public interface ProjectVersionDAO {


    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId AND hash = :hash AND version_string = :versionString")
    ProjectVersionTable getProjectVersionTable(long projectId, String hash, String versionString);
}
