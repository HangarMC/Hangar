package io.papermc.hangar.db.dao.internal.table.versions.downloads;

import io.papermc.hangar.model.db.versions.downloads.ProjectVersionUnsafeDownloadTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionUnsafeDownloadTable.class)
public interface ProjectVersionUnsafeDownloadsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_unsafe_downloads (created_at, user_id, address) VALUES (:now, :userId, :address)")
    ProjectVersionUnsafeDownloadTable insert(@BindBean ProjectVersionUnsafeDownloadTable projectVersionUnsafeDownloadTable);
}
