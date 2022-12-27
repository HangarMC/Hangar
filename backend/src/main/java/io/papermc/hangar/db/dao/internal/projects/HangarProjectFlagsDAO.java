package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarProjectFlag.class)
public interface HangarProjectFlagsDAO {


    @SqlQuery("SELECT pf.*, fu.name reported_by_name, ru.name resolved_by_name, p.owner_name project_owner_name, p.slug project_slug, p.visibility project_visibility " +
        "FROM project_flags pf " +
        "   JOIN projects p ON pf.project_id = p.id " +
        "   JOIN users fu ON pf.user_id = fu.id " +
        "   LEFT JOIN users ru ON ru.id = pf.resolved_by " +
        "WHERE pf.id = :flagId " +
        "GROUP BY pf.id, fu.id, ru.id, p.id")
    HangarProjectFlag getById(long flagId);

    @SqlQuery("SELECT pf.*, fu.name reported_by_name, ru.name resolved_by_name, p.owner_name project_owner_name, p.slug project_slug, p.visibility project_visibility " +
        "FROM project_flags pf " +
        "   JOIN projects p ON pf.project_id = p.id " +
        "   JOIN users fu ON pf.user_id = fu.id " +
        "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
        "WHERE pf.project_id = :projectId " +
        "GROUP BY pf.id, fu.id, ru.id, p.id")
    List<HangarProjectFlag> getFlags(long projectId);

    @SqlQuery("SELECT pf.*, fu.name reported_by_name, ru.name resolved_by_name, p.owner_name project_owner_name, p.slug project_slug, p.visibility project_visibility " +
        "FROM project_flags pf " +
        "   JOIN projects p ON pf.project_id = p.id " +
        "   JOIN users fu ON pf.user_id = fu.id " +
        "   LEFT OUTER JOIN users ru ON ru.id = pf.resolved_by " +
        "WHERE pf.resolved = :resolved " +
        "GROUP BY pf.id, fu.id, ru.id, p.id <offsetLimit>")
    List<HangarProjectFlag> getFlags(@BindPagination RequestPagination pagination, boolean resolved);

    @SqlQuery("SELECT count(id) FROM project_flags WHERE resolved = :resolved")
    long getFlagsCount(boolean resolved);
}
