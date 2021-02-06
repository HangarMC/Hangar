package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectPagesTable;
import io.papermc.hangar.modelold.viewhelpers.ProjectPage;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;

@Repository
@RegisterBeanMapper(ProjectPagesTable.class)
@RegisterBeanMapper(ProjectPage.class)
public interface ProjectPageDao {

    @SqlUpdate("INSERT INTO project_pages (created_at, project_id, name, slug, contents, deletable, parent_id) VALUES (:now, :projectId, :name, :slug, :contents, :deletable, :parentId)")
    @Timestamped
    @GetGeneratedKeys
    ProjectPagesTable insert(@BindBean ProjectPagesTable projectPagesTable);

    @SqlUpdate("UPDATE project_pages SET contents = :contents WHERE id = :id")
    @GetGeneratedKeys
    ProjectPagesTable update(@BindBean ProjectPagesTable projectPagesTable);

    @SqlUpdate("DELETE FROM project_pages WHERE id = :id")
    void delete(@BindBean ProjectPagesTable projectPagesTable);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND (lower(slug) = lower(:pageName) OR id = :pageId)")
    ProjectPage getPage(long projectId, String pageName, Long pageId);

    @SqlQuery("SELECT pp.* FROM project_pages pp JOIN projects p ON pp.project_id = p.id WHERE p.slug = :slug AND p.owner_name = :author")
    List<ProjectPage> getPages(String author, String slug);

    @SqlQuery("WITH RECURSIVE parents AS (SELECT * FROM project_pages WHERE project_id = :projectId AND (name = :pageName OR id = :pageId) UNION SELECT pp.* FROM project_pages pp INNER JOIN parents par ON par.id = pp.parent_id) SELECT * FROM parents")
    List<ProjectPage> getPageParents(long projectId, String pageName, Long pageId);

    @RegisterBeanMapper(ProjectPage.class)
    @KeyColumn("id")
    @SqlQuery("SELECT id, created_at, name, slug, contents, deletable FROM project_pages WHERE project_id = :projectId AND parent_id IS NULL ORDER BY created_at")
    LinkedHashMap<Long, ProjectPage> getRootPages(long projectId);

    @RegisterBeanMapper(ProjectPage.class)
    @SqlQuery("SELECT id, created_at, name, slug, contents, deletable, parent_id FROM project_pages WHERE project_id = :projectId AND parent_id = :parentId")
    List<ProjectPage> getChildPages(long projectId, long parentId);
}
