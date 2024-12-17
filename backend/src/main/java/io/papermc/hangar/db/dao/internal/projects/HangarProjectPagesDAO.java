package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(ExtendedProjectPage.class)
public interface HangarProjectPagesDAO {

    @SqlQuery("SELECT pp.*," +
        "   exists(SELECT 1 FROM project_home_pages php WHERE php.page_id = pp.id AND php.project_id = :projectId) AS home" +
        "   FROM project_pages pp" +
        "       WHERE pp.project_id = :projectId" +
        "   ORDER BY created_at")
    List<ExtendedProjectPage> getProjectPages(long projectId);

    @SqlQuery("SELECT pp.*," +
        "   exists(SELECT 1 FROM project_home_pages php WHERE php.page_id = pp.id AND php.project_id = p.id) AS home" +
        "   FROM project_pages pp" +
        "       JOIN projects p ON pp.project_id = p.id" +
        "   WHERE p.id = projectId AND lower(pp.slug) = lower(:pageSlug)")
    ExtendedProjectPage getProjectPage(long projectId, String pageSlug);

    @SqlQuery("SELECT pp.*, TRUE AS home " +
            "   FROM project_pages pp" +
            "       JOIN project_home_pages php ON pp.id = php.page_id" +
            "   WHERE pp.project_id = :projectId")
    ExtendedProjectPage getHomePage(long projectId);

    @SqlQuery("SELECT pp.*," +
        "   exists(SELECT 1 FROM project_home_pages php WHERE php.page_id = pp.id AND php.project_id = p.id) AS home" +
        "   FROM project_pages pp" +
        "       JOIN projects p ON pp.project_id = p.id" +
        "   WHERE pp.id = :id")
    ExtendedProjectPage getProjectPage(long id);

    @SqlQuery("""
        SELECT pp.*, true as home
        FROM project_pages pp
            JOIN project_home_pages php ON pp.id = php.page_id
        WHERE php.project_id = :projectId
    """)
    ExtendedProjectPage getProjectHomePage(long projectId);
}
