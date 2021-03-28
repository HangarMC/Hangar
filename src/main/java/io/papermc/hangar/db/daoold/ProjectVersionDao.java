package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(forRemoval = true)
@RegisterBeanMapper(ProjectVersionsTable.class)
public interface ProjectVersionDao {

    @SqlUpdate("UPDATE project_versions SET visibility = :visibility, reviewer_id = :reviewerId, approved_at = :approvedAt, description = :description, " +
               "review_state = :reviewState, external_url = :externalUrl " +
               "WHERE id = :id")
    void update(@BindBean ProjectVersionsTable projectVersionsTable);

    @SqlUpdate("DELETE FROM project_versions WHERE id = :id")
    void deleteVersion(long id);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId ORDER BY created_at DESC")
    List<ProjectVersionsTable> getProjectVersions(long projectId);

}
