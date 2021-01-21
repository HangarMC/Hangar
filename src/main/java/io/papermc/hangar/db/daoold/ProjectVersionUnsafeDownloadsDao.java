package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectVersionUnsafeDownloadsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(ProjectVersionUnsafeDownloadsTable.class)
public interface ProjectVersionUnsafeDownloadsDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_unsafe_downloads (created_at, user_id, address, download_type) VALUES (:now, :userId, :address, :downloadType)")
    ProjectVersionUnsafeDownloadsTable insert(@BindBean ProjectVersionUnsafeDownloadsTable projectVersionUnsafeDownloadsTable);
}
