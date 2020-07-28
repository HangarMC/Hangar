package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.service.project.ProjectFactory.InvalidProject;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.ProjectsTable;

@Repository
@RegisterBeanMapper(ProjectsTable.class)
public interface ProjectDao {

    @SqlUpdate("insert into projects (created_at, plugin_id, name, slug, owner_name, owner_id, category, description, visibility) values (:now, :pluginId, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility)")
    @Timestamped
    @GetGeneratedKeys
    ProjectsTable insert(@BindBean ProjectsTable project);

    @SqlQuery("SELECT CASE WHEN owner_id = :ownerId AND name = :name THEN 'OWNER_NAME' WHEN owner_id = :ownerId AND slug = :slug THEN 'OWNER_SLUG' WHEN plugin_id = :pluginId THEN 'PLUGIN_ID' END FROM projects")
    InvalidProject checkValidProject(long ownerId, String pluginId, String name, String slug);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectsTable getBySlug(String author, String slug);
}
