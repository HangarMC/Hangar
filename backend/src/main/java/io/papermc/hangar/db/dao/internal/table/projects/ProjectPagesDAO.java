package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectHomePageTable;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectPageTable.class)
public interface ProjectPagesDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_pages (created_at, project_id, name, slug, contents, deletable, parent_id) VALUES (:now, :projectId, :name, :slug, :contents, :deletable, :parentId)")
    ProjectPageTable insert(@BindBean ProjectPageTable projectPageTable);

    @SqlUpdate("UPDATE project_pages SET contents = :contents WHERE id = :id")
    void update(@BindBean ProjectPageTable projectPageTable);

    @SqlBatch("UPDATE project_pages SET parent_id = :parentId WHERE id = :id")
    void updateParents(@BindBean Collection<ProjectPageTable> projectPageTables);

    @SqlUpdate("DELETE FROM project_pages WHERE id = :id")
    void delete(@BindBean ProjectPageTable projectPageTable);

    @Timestamped
    @SqlUpdate("INSERT INTO project_home_pages (created_at, project_id, page_id) VALUES (:now, :projectId, :pageId)")
    void insertHomePage(@BindBean ProjectHomePageTable projectHomePageTable);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id = :parentId")
    List<ProjectPageTable> getChildPages(long projectId, long parentId);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id = :parentId AND name = :name")
    ProjectPageTable getChildPage(long projectId, long parentId, String name);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND parent_id IS NULL AND lower(slug) = lower(:slug)")
    ProjectPageTable getRootPage(long projectId, String slug);

    @SqlQuery("SELECT * FROM project_pages WHERE project_id = :projectId AND id = :pageId")
    ProjectPageTable getProjectPage(long projectId, long pageId);
}
