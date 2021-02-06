package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectPageTable;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RegisterConstructorMapper(ProjectPageTable.class)
public interface ProjectPagesDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_pages (created_at, project_id, name, slug, deletable, parent_id) VALUES (:now, :projectId, :name, :slug, :deletable, :parentId)")
    ProjectPageTable insert(@BindBean ProjectPageTable projectPageTable);

    @KeyColumn("id")
    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id IS NULL")
    Map<Long, ProjectPageTable> getRootPages(long projectId);
}
