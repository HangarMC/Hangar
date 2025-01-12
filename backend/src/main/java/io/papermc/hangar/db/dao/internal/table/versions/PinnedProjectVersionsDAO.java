package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.PinnedProjectVersionTable;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
public interface PinnedProjectVersionsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO pinned_project_versions (created_at, project_id, version_id) VALUES (:now, :projectId, :versionId) ON CONFLICT DO NOTHING")
    void insert(@BindBean PinnedProjectVersionTable pinnedProjectVersionTable);

    @SqlUpdate("DELETE FROM pinned_project_versions WHERE project_id = :projectId AND version_id = :versionId")
    void deleteVersion(long projectId, Long versionId);
}
