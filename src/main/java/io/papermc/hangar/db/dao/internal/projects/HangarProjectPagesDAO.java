package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.model.internal.projects.HangarViewProjectPage;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(HangarViewProjectPage.class)
public interface HangarProjectPagesDAO {

    @SqlQuery("SELECT pp.*," +
            "   exists(SELECT 1 FROM project_home_pages php WHERE php.page_id = pp.id AND php.project_id = :projectId) AS home" +
            "   FROM project_pages pp" +
            "       WHERE pp.project_id = :projectId" +
            "   ORDER BY created_at")
    List<HangarViewProjectPage> getProjectPages(long projectId);

    @SqlQuery("SELECT pp.*," +
            "   exists(SELECT 1 FROM project_home_pages php WHERE php.page_id = pp.id AND php.project_id = p.id) AS home" +
            "   FROM project_pages pp" +
            "       JOIN projects p ON pp.project_id = p.id" +
            "   WHERE lower(p.owner_name) = lower(:author) AND lower(p.slug) = lower(:slug) AND pp.slug = :pageSlug")
    HangarViewProjectPage getProjectPage(String author, String slug, String pageSlug);

    @SqlQuery("SELECT pp.*, TRUE AS home " +
            "   FROM project_pages pp" +
            "       JOIN projects p ON pp.project_id = p.id" +
            "       JOIN project_home_pages php ON pp.id = php.page_id" +
            "   WHERE lower(p.owner_name) = lower(:author) AND lower(p.slug) = lower(:slug)")
    HangarViewProjectPage getHomePage(String author, String slug);
}
