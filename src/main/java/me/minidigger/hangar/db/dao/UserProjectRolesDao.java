package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.UserProjectRolesTable;

import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(UserProjectRolesTable.class)
public interface UserProjectRolesDao {

    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    UserProjectRolesTable getByProjectAndUser(long projectId, long userId);

    @SqlUpdate("INSERT INTO user_project_roles (created_at, user_id, role_type, project_id, is_accepted) " +
               "VALUES (:now, :userId, :roleType, :projectId, :isAccepted)")
    @Timestamped
    void insert(@BindBean UserProjectRolesTable userProjectRolesTable);

    @RegisterBeanMapper(ProjectsTable.class)
    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "upr")
    @SqlQuery("SELECT p.*, upr.id upr_id, upr.created_at upr_created_at, upr.user_id upr_user_id, upr.role_type upr_role_type, upr.project_id upr_project_id, upr.is_accepted upr_is_accepted FROM user_project_roles upr LEFT OUTER JOIN projects p ON upr.project_id = p.id WHERE user_id = :userId AND upr.is_accepted = false ORDER BY upr.created_at")
    Map<UserProjectRolesTable, ProjectsTable> getUnacceptedRoles(long userId);
}
