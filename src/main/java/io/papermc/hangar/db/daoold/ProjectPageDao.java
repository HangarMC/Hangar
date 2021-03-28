package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectPagesTable;
import io.papermc.hangar.modelold.viewhelpers.ProjectPage;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectPagesTable.class)
@RegisterBeanMapper(ProjectPage.class)
@Deprecated(forRemoval = true)
public interface ProjectPageDao {
    @SqlQuery("SELECT pp.* FROM project_pages pp JOIN projects p ON pp.project_id = p.id WHERE p.slug = :slug AND p.owner_name = :author")
    List<ProjectPage> getPages(String author, String slug);
}
