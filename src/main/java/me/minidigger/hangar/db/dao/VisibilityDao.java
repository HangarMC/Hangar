package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectVersionVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectVisibilityChangesTable;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RegisterBeanMapper(ProjectVisibilityChangesTable.class)
@RegisterBeanMapper(ProjectVersionVisibilityChangesTable.class)
public interface VisibilityDao {

    @GetGeneratedKeys
    @Timestamped
    @SqlUpdate("INSERT INTO project_visibility_changes (created_at, created_by, project_id, comment, resolved_at, resolved_by, visibility) " +
            "VALUES (:now, :createdBy, :projectId, :comment, :resolvedAt, :resolvedBy, :visibility)")
    ProjectVisibilityChangesTable insert(@BindBean ProjectVisibilityChangesTable projectVisibilityChangesTable);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_visibility_changes (created_at, created_by, version_id, comment, resolved_at, resolved_by, visibility) " +
            "VALUES (:now, :createdBy, :versionId, :comment, :resolvedAt, :resolvedBy, :visibility)")
    ProjectVersionVisibilityChangesTable insert(@BindBean ProjectVersionVisibilityChangesTable projectVersionVisibilityChangesTable);

    @SqlUpdate("UPDATE project_visibility_changes SET resolved_at = :resolvedAt, resolved_by = :resolvedBy")
        // I think these are the only two things that change after the fact
    void update(@BindBean ProjectVisibilityChangesTable projectVisibilityChangesTable);

    @KeyColumn("user_name")
    @SqlQuery("SELECT pvc.*, u.name user_name FROM project_visibility_changes pvc LEFT OUTER JOIN users u ON pvc.created_by = u.id WHERE project_id = :projectId ORDER BY created_at DESC LIMIT 1")
    Map.Entry<String, ProjectVisibilityChangesTable> getLatestProjectVisibilityChange(long projectId);

    @ValueColumn("user_name")
    @SqlQuery("SELECT pvc.*, u.name user_name FROM project_version_visibility_changes pvc RIGHT OUTER JOIN users u ON pvc.created_by = u.id WHERE version_id = :versionId ORDER BY created_at")
    Map<ProjectVersionVisibilityChangesTable, String> getProjectVersionVisibilityChanges(long versionId);

    @Timestamped
    @SqlUpdate("UPDATE project_visibility_changes " +
            "SET resolved_at = :now, resolved_by = :userId " +
            "FROM " +
            "(SELECT project_id FROM project_visibility_changes WHERE project_id = :projectId AND resolved_at IS NULL AND resolved_by IS NULL ORDER BY created_at LIMIT 1) AS subquery " +
            "WHERE project_visibility_changes.project_id=subquery.project_id")
    void updateLatestProjectChange(long userId, long projectId);

    @Timestamped
    @SqlUpdate("UPDATE project_version_visibility_changes " +
            "SET resolved_at = :now, resolved_by = :userId " +
            "FROM" +
            "    (SELECT version_id FROM project_version_visibility_changes WHERE version_id = :versionId AND resolved_at IS NULL AND resolved_by IS NULL ORDER BY created_at LIMIT 1) as subquery " +
            "WHERE project_version_visibility_changes.version_id = subquery.version_id")
    void updateLatestVersionChange(long userId, long versionId);

}
