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

import java.util.List;
import java.util.Map;

@Repository
@RegisterConstructorMapper(ProjectPageTable.class)
public interface ProjectPagesDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_pages (created_at, project_id, name, slug, contents, deletable, parent_id) VALUES (:now, :projectId, :name, :slug, :contents, :deletable, :parentId)")
    ProjectPageTable insert(@BindBean ProjectPageTable projectPageTable);

    @SqlUpdate("UPDATE project_pages SET contents = :contents WHERE id = :id")
    void update(@BindBean ProjectPageTable projectPageTable);

    @SqlUpdate("DELETE FROM project_pages WHERE id = :id")
    void delete(@BindBean ProjectPageTable projectPageTable);

    @KeyColumn("id")
    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id IS NULL")
    Map<Long, ProjectPageTable> getRootPages(long projectId);

    @SqlQuery("SELECT slug FROM project_pages WHERE project_id = :projectId AND parent_id = :parentId")
    List<String> getChildPages(long projectId, long parentId);

    @SqlQuery("SELECT pp.* FROM project_pages pp" +
            "   WHERE pp.project_id = :projectId" +
            "   ORDER BY created_at")
    List<ProjectPageTable> getProjectPages(long projectId);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND id = :pageId")
    ProjectPageTable getProjectPage(long projectId, long pageId);

    @SqlQuery("SELECT pp.* FROM project_pages pp" +
            "   JOIN projects p ON pp.project_id = p.id" +
            "   WHERE p.owner_name = :author AND p.slug = :slug AND pp.slug = :pageSlug")
    ProjectPageTable getProjectPage(String author, String slug, String pageSlug);
}
