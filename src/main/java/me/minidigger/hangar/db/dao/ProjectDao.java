package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.model.generated.ProjectStatsAll;
import me.minidigger.hangar.service.project.ProjectFactory.InvalidProjectReason;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectsTable.class)
public interface ProjectDao {

    @SqlUpdate("insert into projects (created_at, plugin_id, name, slug, owner_name, owner_id, category, description, visibility) " +
               "values (:now, :pluginId, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility)")
    @Timestamped
    @GetGeneratedKeys
    ProjectsTable insert(@BindBean ProjectsTable project);

    @SqlQuery("SELECT CASE " +
              "WHEN owner_id = :ownerId AND name = :name THEN 'OWNER_NAME' " +
              "WHEN owner_id = :ownerId AND slug = :slug THEN 'OWNER_SLUG' " +
              "WHEN plugin_id = :pluginId THEN 'PLUGIN_ID' " +
              "END " +
              "FROM projects"
    )
    InvalidProjectReason checkValidProject(long ownerId, String pluginId, String name, String slug);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectsTable getBySlug(String author, String slug);

    @SqlQuery("SELECT * FROM projects WHERE plugin_id = :pluginId")
    ProjectsTable getByPluginId(String pluginId);

    @SqlQuery("SELECT COUNT(*) FROM projects WHERE owner_id = :id")
    int getProjectCountByUserId(long id);

    @SqlQuery("SELECT * FROM projects WHERE owner_id = :id")
    List<ProjectsTable> getProjectsByUserId(long id);

    @RegisterBeanMapper(ProjectStatsAll.class)
    @SqlQuery("SELECT * FROM " +
              "(SELECT COUNT(*) as watchers FROM project_watchers pw WHERE pw.project_id = :id) as w, " +
              "(SELECT COUNT(*) as stars FROM project_stars ps WHERE ps.project_id = :id) as s, " +
              "(SELECT COUNT(*) as views FROM project_views_individual pvi WHERE pvi.project_id = :id) as v, " +
              "(SELECT COUNT(*) as downloads FROM project_versions_downloads_individual pvdi WHERE pvdi.project_id = :id) as d"
    )
    ProjectStatsAll getProjectStats(long id);
}
