package io.papermc.hangar.db.dao.internal.table.versions.dependencies;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.dependencies.ProjectVersionPlatformDependencyTable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionPlatformDependencyTable.class)
public interface ProjectVersionPlatformDependenciesDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlBatch("INSERT INTO project_version_platform_dependencies (created_at, version_id, platform_version_id) VALUES (:now, :versionId, :platformVersionId)")
    List<ProjectVersionPlatformDependencyTable> insertAll(@BindBean Collection<ProjectVersionPlatformDependencyTable> platformDependencyTables);

    @SqlBatch("DELETE FROM project_version_platform_dependencies WHERE id = :id")
    void deleteAll(@BindBean Collection<ProjectVersionPlatformDependencyTable> platformDependencyTables);

    @SqlQuery("SELECT * FROM project_version_platform_dependencies WHERE version_id = :versionId")
    List<ProjectVersionPlatformDependencyTable> getForVersion(long versionId);

    @KeyColumn("version")
    @SqlQuery("SELECT pvpd.*, pv.version" +
        "   FROM project_version_platform_dependencies pvpd" +
        "       JOIN platform_versions pv ON pvpd.platform_version_id = pv.id" +
        "   WHERE pvpd.version_id = :versionId AND pv.platform = :platform")
    Map<String, ProjectVersionPlatformDependencyTable> getPlatformVersions(long versionId, @EnumByOrdinal Platform platform);
}
