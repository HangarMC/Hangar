package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectFlagsTable;
import me.minidigger.hangar.model.viewhelpers.ProjectFlag;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(ProjectFlagsTable.class)
public interface FlagDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_flags (created_at, project_id, user_id, reason, comment) VALUES (:now, :projectId, :userId, :reason, :comment)")
    ProjectFlagsTable insert(@BindBean ProjectFlagsTable projectFlagsTable);

    @RegisterBeanMapper(value = ProjectFlagsTable.class, prefix = "p")
    @RegisterBeanMapper(value = ProjectFlag.class, prefix = "pf")
    @SqlQuery("SELECT pf.id p_id, pf.created_at p_created_at, pf.project_id p_project_id, pf.user_id p_user_id, pf.reason p_reason, pf.is_resolved p_is_resolved, pf.comment p_comment, pf.resolved_at p_resolved_at, pf.resolved_by p_resolved_by, " +
            "fu.name pf_reported_by, ru.name pf_resolved_by, p.owner_name pf_project_owner_name, p.slug pf_project_slug, p.visibility pf_project_visibility " +
            "FROM project_flags pf " +
            "   JOIN projects p ON pf.project_id = p.id " +
            "   JOIN users fu ON pf.user_id = fu.id " +
            "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
            "WHERE pf.id = :flagId")
    Map<ProjectFlag, ProjectFlagsTable> getById(long flagId);

    @SqlUpdate("UPDATE project_flags SET is_resolved = :resolved, resolved_by = :resolvedBy, resolved_at = :resolvedAt WHERE id = :flagId")
    @GetGeneratedKeys
    ProjectFlagsTable markAsResolved(long flagId, boolean resolved, Long resolvedBy, OffsetDateTime resolvedAt);

    @SqlQuery("SELECT * FROM project_flags WHERE user_id = :userId AND project_id = :projectId AND is_resolved = false")
    ProjectFlagsTable getUnresolvedFlag(long projectId, long userId);

    @RegisterBeanMapper(value = ProjectFlagsTable.class, prefix = "p")
    @RegisterBeanMapper(value = ProjectFlag.class, prefix = "pf")
    @SqlQuery("SELECT pf.id p_id, pf.created_at p_created_at, pf.project_id p_project_id, pf.user_id p_user_id, pf.reason p_reason, pf.is_resolved p_is_resolved, pf.comment p_comment, pf.resolved_at p_resolved_at, pf.resolved_by p_resolved_by, " +
            "u.name pf_reported_by, ru.name pf_resolved_by, p.owner_name pf_project_owner_name, p.slug pf_project_slug, p.visibility pf_project_visibility " +
            "FROM project_flags pf " +
            "   JOIN users u ON u.id = pf.user_id " +
            "   JOIN projects p ON pf.project_id = p.id" +
            "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
            "WHERE pf.project_id = :projectId " +
            "ORDER BY pf.created_at")
    Map<ProjectFlag, ProjectFlagsTable> getProjectFlags(long projectId);

    @RegisterBeanMapper(value = ProjectFlagsTable.class, prefix = "p")
    @RegisterBeanMapper(value = ProjectFlag.class, prefix = "pf")
    @SqlQuery("SELECT pf.id p_id, pf.created_at p_created_at, pf.project_id p_project_id, pf.user_id p_user_id, pf.reason p_reason, pf.is_resolved p_is_resolved, pf.comment p_comment, pf.resolved_at p_resolved_at, pf.resolved_by p_resolved_by, " +
            "fu.name pf_reported_by, ru.name pf_resolved_by, p.owner_name pf_project_owner_name, p.slug pf_project_slug, p.visibility pf_project_visibility " +
            "FROM project_flags pf " +
            "   JOIN projects p ON pf.project_id = p.id " +
            "   JOIN users fu ON pf.user_id = fu.id " +
            "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
            "WHERE NOT pf.is_resolved " +
            "GROUP BY pf.id, fu.id, ru.id, p.id")
    Map<ProjectFlag, ProjectFlagsTable> getFlags();


}
