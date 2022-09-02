package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.PinnedProjectVersionTable;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface PinnedProjectVersionsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO pinned_project_versions (created_at, project_id, version_id) VALUES (:now, :projectId, :versionId) ON CONFLICT DO NOTHING")
    void insert(@BindBean PinnedProjectVersionTable pinnedProjectVersionTable);

    @SqlUpdate("DELETE FROM pinned_project_versions WHERE project_id = :projectId AND (id = :ppvId OR version_id = :versionId)")
    void _delete(long projectId, @Nullable Long ppvId, @Nullable Long versionId);

    default void deletePinnedProjectVersion(final long projectId, final long ppvId) {
        this._delete(projectId, ppvId, null);
    }

    default void deleteVersion(final long projectId, final long versionId) {
        this._delete(projectId, null, versionId);
    }
}
