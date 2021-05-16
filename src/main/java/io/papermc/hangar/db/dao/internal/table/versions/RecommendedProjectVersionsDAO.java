package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.RecommendedProjectVersionTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface RecommendedProjectVersionsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO recommended_project_versions (created_at, version_id, project_id, platform) VALUES (:now, :versionId, :projectId, :platform)")
    void insert(@BindBean RecommendedProjectVersionTable recommendedProjectVersionTable);

    @SqlUpdate("DELETE FROM recommended_project_versions WHERE project_id = :projectId AND platform = :platform")
    void delete(long projectId, @EnumByOrdinal Platform platform);

    @KeyColumn("platform")
    @ValueColumn("version_id")
    @SqlQuery("SELECT platform, version_id FROM recommended_project_versions WHERE project_id = :projectId ORDER BY platform")
    Map<Platform, Long> getRecommendedVersions(long projectId);

    @KeyColumn("platform")
    @ValueColumn("version_string")
    @SqlQuery("SELECT platform, version_string " +
              "FROM recommended_project_versions rpv " +
              "     JOIN projects p on p.id = rpv.project_id " +
              "     JOIN project_versions pv on rpv.version_id = pv.id " +
              "WHERE p.owner_name = :owner AND p.slug = :slug " +
              "ORDER BY platform")
    Map<Platform, String> getRecommendedVersions(String owner, String slug);
}
