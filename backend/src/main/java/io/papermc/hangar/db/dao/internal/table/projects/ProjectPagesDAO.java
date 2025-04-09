package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectPageTable;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(ProjectPageTable.class)
public interface ProjectPagesDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_pages (created_at, project_id, name, slug, contents, deletable, parent_id, homepage) VALUES (:now, :projectId, :name, :slug, :contents, :deletable, :parentId, :homepage)")
    ProjectPageTable insert(@BindBean ProjectPageTable projectPageTable);

    @SqlUpdate("UPDATE project_pages SET contents = :contents WHERE id = :id")
    void update(@BindBean ProjectPageTable projectPageTable);

    @SqlBatch("UPDATE project_pages SET parent_id = :parentId WHERE id = :id")
    void updateParents(@BindBean Collection<ProjectPageTable> projectPageTables);

    @SqlUpdate("DELETE FROM project_pages WHERE id = :id")
    void delete(@BindBean ProjectPageTable projectPageTable);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id = :parentId")
    List<ProjectPageTable> getChildPages(long projectId, long parentId);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id = :parentId AND name = :name")
    ProjectPageTable getChildPage(long projectId, long parentId, String name);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id IS NULL AND lower(slug) = lower(:slug)")
    ProjectPageTable getRootPage(long projectId, String slug);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND id = :pageId")
    ProjectPageTable getProjectPage(long projectId, long pageId);

    @SqlQuery("""
        SELECT pp.*
           FROM project_pages pp
               WHERE pp.project_id = :projectId
           ORDER BY created_at
        """)
    List<ProjectPageTable> getProjectPages(long projectId);

    @SqlQuery("""
        SELECT pp.*
           FROM project_pages pp
           WHERE lower(pp.slug) = lower(:pageSlug)
        """)
    ProjectPageTable getProjectPage(long projectId, String pageSlug);

    @SqlQuery("""
        SELECT pp.*
           FROM project_pages pp
           WHERE pp.project_id = :projectId AND homepage = true
        """)
    ProjectPageTable getHomePage(long projectId);
}
