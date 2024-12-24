package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.api.project.ProjectCompact;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
public interface PinnedProjectDAO {

    @SqlUpdate("INSERT INTO pinned_user_projects (project_id, user_id) VALUES (:projectId, :userId) ON CONFLICT DO NOTHING")
    void insert(long userId, long projectId);

    @SqlUpdate("DELETE FROM pinned_user_projects WHERE project_id = :projectId AND user_id = :userId")
    void delete(long userId, long projectId);

    @SqlQuery("SELECT * FROM pinned_projects WHERE user_id = :userId")
    @RegisterConstructorMapper(ProjectCompact.class)
    List<ProjectCompact> pinnedProjects(long userId);
}
