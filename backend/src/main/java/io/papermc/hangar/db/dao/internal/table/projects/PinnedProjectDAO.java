package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.api.project.ProjectCompact;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
public interface PinnedProjectDAO {

    @SqlUpdate("INSERT INTO pinned_user_projects (project_id, user_id) VALUES ((SELECT id FROM projects WHERE lower(slug) = lower(:slug)), :userId) ON CONFLICT DO NOTHING")
    void insert(long userId, String slug);

    @SqlUpdate("DELETE FROM pinned_user_projects WHERE project_id = (SELECT id FROM projects WHERE lower(slug) = lower(:slug)) AND user_id = :userId")
    void delete(long userId, String slug);

    @SqlQuery("SELECT * FROM pinned_projects WHERE user_id = :userId")
    @RegisterConstructorMapper(ProjectCompact.class)
    List<ProjectCompact> pinnedProjects(long userId);
}
