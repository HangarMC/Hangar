package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import java.time.OffsetDateTime;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectFlagTable.class)
public interface ProjectFlagsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_flags (created_at, project_id, user_id, reason, comment) VALUES (:now, :projectId, :userId, :reason, :comment)")
    ProjectFlagTable insert(@BindBean ProjectFlagTable projectFlagsTable);

    @SqlUpdate("UPDATE project_flags SET resolved = :resolved, resolved_by = :resolvedBy, resolved_at = :resolvedAt WHERE id = :flagId")
    void markAsResolved(long flagId, boolean resolved, Long resolvedBy, OffsetDateTime resolvedAt);

    @SqlQuery("SELECT * FROM project_flags WHERE project_id = :projectId AND user_id = :userId AND resolved IS FALSE")
    ProjectFlagTable getUnresolvedFlag(long projectId, long userId);
}
