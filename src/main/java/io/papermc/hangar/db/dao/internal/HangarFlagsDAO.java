package io.papermc.hangar.db.dao.internal;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;

@Repository
public interface HangarFlagsDAO {

    @Timestamped
    @GetGeneratedKeys
    @RegisterConstructorMapper(ProjectFlagTable.class)
    @SqlUpdate("INSERT INTO project_flags (created_at, project_id, user_id, reason, comment) VALUES (:now, :projectId, :userId, :reason, :comment)")
    ProjectFlagTable insert(@BindBean ProjectFlagTable projectFlagsTable);

    @RegisterConstructorMapper(HangarProjectFlag.class)
    @SqlQuery("SELECT pf.*, fu.name reported_by_name, ru.name resolved_by_name, p.owner_name project_owner_name, p.slug project_slug, p.visibility project_visibility " +
              "FROM project_flags pf " +
              "   JOIN projects p ON pf.project_id = p.id " +
              "   JOIN users fu ON pf.user_id = fu.id " +
              "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
              "WHERE lower(p.owner_name) = lower(:author) AND lower(p.slug) = lower(:slug)" +
              "GROUP BY pf.id, fu.id, ru.id, p.id")
    List<HangarProjectFlag> getFlags(String author, String slug);

    @RegisterConstructorMapper(HangarProjectFlag.class)
    @SqlQuery("SELECT pf.*, fu.name reported_by_name, ru.name resolved_by_name, p.owner_name project_owner_name, p.slug project_slug, p.visibility project_visibility " +
              "FROM project_flags pf " +
              "   JOIN projects p ON pf.project_id = p.id " +
              "   JOIN users fu ON pf.user_id = fu.id " +
              "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
              "WHERE NOT pf.resolved " +
              "GROUP BY pf.id, fu.id, ru.id, p.id")
    List<HangarProjectFlag> getFlags();

    @SqlUpdate("UPDATE project_flags SET resolved = :resolved, resolved_by = :resolvedBy, resolved_at = :resolvedAt WHERE id = :flagId")
    @GetGeneratedKeys
    @RegisterConstructorMapper(ProjectFlagTable.class)
    ProjectFlagTable markAsResolved(long flagId, boolean resolved, Long resolvedBy, OffsetDateTime resolvedAt);
}
