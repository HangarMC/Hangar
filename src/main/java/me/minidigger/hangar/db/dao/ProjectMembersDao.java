package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.ProjectMembersTable;

@Repository
@RegisterBeanMapper(ProjectMembersTable.class)
public interface ProjectMembersDao {

    @SqlUpdate("INSERT INTO project_members VALUES (:projectId, :userId)")
    void insert(@BindBean ProjectMembersTable entry);

    @SqlUpdate("DELETE FROM project_members WHERE project_id = :projectId AND user_id = :userId")
    int delete(long projectId, long userId);
}
