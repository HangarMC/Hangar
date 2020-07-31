package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectVersionVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(ProjectVisibilityChangesTable.class)
@RegisterBeanMapper(ProjectVersionVisibilityChangesTable.class)
public interface VisibilityDao {

    @GetGeneratedKeys
    @Timestamped
    @SqlUpdate("INSERT INTO project_visibility_changes (created_at, created_by, project_id, comment, resolved_at, resolved_by, visibility) " +
            "VALUES (:now, :createdBy, :projectId, :comment, :resolvedAt, :resolvedBy, :visibility)")
    ProjectVisibilityChangesTable insert(@BindBean ProjectVisibilityChangesTable projectVisibilityChangesTable);

    @SqlUpdate("UPDATE project_visibility_changes SET resolved_at = :resolvedAt, resolved_by = :resolvedBy") // I think these are the only two things that change after the fact
    void update(@BindBean ProjectVisibilityChangesTable projectVisibilityChangesTable);

    @SqlQuery("SELECT * FROM project_visibility_changes WHERE project_id = :projectId AND resolved_at IS NULL ORDER BY created_at LIMIT 1")
    ProjectVisibilityChangesTable getLatestProjectVisibilityChange(long projectId);

    @Timestamped
    @SqlUpdate("UPDATE project_visibility_changes " +
            "SET resolved_at = :now, resolved_by = :userId " +
            "FROM " +
            "(SELECT project_id FROM project_visibility_changes WHERE project_id = :projectId AND resolved_at IS NULL AND resolved_by IS NULL ORDER BY created_at LIMIT 1) AS subquery " +
            "WHERE project_visibility_changes.project_id=subquery.project_id")
    void updateLatestChange(long userId, long projectId);

}
