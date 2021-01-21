package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RegisterBeanMapper(UserProjectRolesTable.class)
public interface UserProjectRolesDao {

    @Timestamped
    @SqlUpdate("INSERT INTO user_project_roles (created_at, user_id, role_type, project_id, is_accepted) " +
               "VALUES (:now, :userId, :roleType, :projectId, :isAccepted)")
    void insert(@BindBean UserProjectRolesTable userProjectRolesTable);

    @SqlUpdate("UPDATE user_project_roles SET role_type = :roleType, is_accepted = :isAccepted WHERE id = :id")
    void update(@BindBean UserProjectRolesTable userProjectRolesTable);

    @SqlUpdate("DELETE FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    void delete(long projectId, long userId);

    @SqlQuery("SELECT * FROM user_project_roles WHERE id = :id")
    UserProjectRolesTable getById(long id);

    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    UserProjectRolesTable getByProjectAndUser(long projectId, long userId);

    @RegisterBeanMapper(ProjectsTable.class)
    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "upr")
    @SqlQuery("SELECT p.*, upr.id upr_id, upr.created_at upr_created_at, upr.user_id upr_user_id, upr.role_type upr_role_type, upr.project_id upr_project_id, upr.is_accepted upr_is_accepted FROM user_project_roles upr LEFT OUTER JOIN projects p ON upr.project_id = p.id WHERE user_id = :userId AND upr.is_accepted = false ORDER BY upr.created_at")
    Map<UserProjectRolesTable, ProjectsTable> getUnacceptedRoles(long userId);
}
