package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.ProjectsTable;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectsTable.class)
public interface ProjectDao {

    @SqlUpdate("insert into projects (id, created_at, plugin_id, name, slug, owner_name, owner_id, category, description, visibility) values (:id, :now, :pluginId, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility)")
    @Timestamped
    @GetGeneratedKeys
    ProjectsTable insert(@BindBean ProjectsTable project);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectsTable getBySlug(String author, String slug);

    @SqlQuery("SELECT COUNT(*) FROM projects WHERE owner_id = :id")
    int getProjectCountByUserId(long id);

    @SqlQuery("SELECT * FROM projects WHERE owner_id = :id")
    List<ProjectsTable> getProjectsByUserId(long id);
}
