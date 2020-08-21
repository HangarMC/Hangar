package io.papermc.hangar.db.dao.api;

import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.generated.Project;
import io.papermc.hangar.model.generated.Tag;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(Project.class)
public interface ProjectApiDao {

    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
              "       p.plugin_id," +
              "       p.name," +
              "       p.owner_name," +
              "       p.slug," +
              "       p.promoted_versions," +
              "       p.views," +
              "       p.downloads," +
              "       p.recent_views," +
              "       p.recent_downloads," +
              "       p.stars," +
              "       p.watchers," +
              "       p.category," +
              "       p.description," +
              "       COALESCE(p.last_updated, p.created_at) AS last_updated," +
              "       p.visibility, " +//"<user_actions_taken>" +
              "       ps.homepage," +
              "       ps.issues," +
              "       ps.source," +
              "       ps.support," +
              "       ps.license_name," +
              "       ps.license_url," +
              "       ps.forum_sync" +
              "  FROM home_projects p" +
              "         JOIN projects ps ON p.id = ps.id") // TODO add all the missing filters
    @AllowUnusedBindings // todo remove
    List<Project> listProjects(String pluginId, List<Category> categories, List<Tag> tags, String query, String owner, boolean seeHidden, Long requesterId, String ordering, long limit, long offset);
}
