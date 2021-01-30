package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RegisterConstructorMapper(ProjectVersionPlatformDependencyTable.class)
public interface ProjectVersionPlatformDependencyDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlBatch("INSERT INTO project_version_platform_dependencies (created_at, version_id, platform_version_id) VALUES (:now, :versionId, :platformVersionId)")
    List<ProjectVersionPlatformDependencyTable> insertAll(@BindBean Collection<ProjectVersionPlatformDependencyTable> platformDependencyTables);

    @SqlQuery("SELECT * FROM project_version_platform_dependencies WHERE version_id = :versionId")
    List<ProjectVersionPlatformDependencyTable> getForVersion(long versionId);
}
