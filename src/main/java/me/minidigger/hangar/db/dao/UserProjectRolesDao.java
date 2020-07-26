package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.UserProjectRolesTable;

@Repository
@RegisterBeanMapper(UserProjectRolesTable.class)
public interface UserProjectRolesDao {

    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    UserProjectRolesTable getByProjectAndUser(long projectId, long userId);

    @SqlUpdate("INSERT INTO user_project_roles (created_at, user_id, role_type, project_id, is_accepted) VALUES (:now, :userId, :roleType, :projectId, :isAccepted)")
    @Timestamped
    void insert(@BindBean UserProjectRolesTable userProjectRolesTable);
}
