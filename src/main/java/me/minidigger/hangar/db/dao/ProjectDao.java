package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.ProjectsTable;

@Repository
@RegisterBeanMapper(ProjectsTable.class)
public interface ProjectDao {

    @SqlUpdate("insert into projects (id, created_at, plugin_id, name, slug, owner_name, owner_id, category, description, visibility) values (:id, :now, :pluginId, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility)")
    @Timestamped
    @GetGeneratedKeys
    ProjectsTable insert(@BindBean ProjectsTable project);
}
