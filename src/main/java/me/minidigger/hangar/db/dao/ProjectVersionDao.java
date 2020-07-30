package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.ProjectVersionsTable;

@Repository
@RegisterBeanMapper(ProjectVersionsTable.class)
public interface ProjectVersionDao {

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId")
    ProjectVersionsTable getVersion(long projectId);
}
