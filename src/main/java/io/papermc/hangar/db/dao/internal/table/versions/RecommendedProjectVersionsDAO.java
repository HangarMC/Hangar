package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.RecommendedProjectVersionTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendedProjectVersionsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO recommended_project_versions (created_at, version_id, project_id, platform) VALUES (:now, :versionId, :projectId, :platform)")
    void insert(@BindBean RecommendedProjectVersionTable recommendedProjectVersionTable);

    @SqlUpdate("DELETE FROM recommended_project_versions WHERE project_id = :projectId AND platform = :platform")
    void delete(long projectId, @EnumByOrdinal Platform platform);
}
