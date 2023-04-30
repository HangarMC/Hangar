package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.db.mappers.factories.RoleColumnMapperFactory;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectRoleTable.class)
@RegisterColumnMapperFactory(RoleColumnMapperFactory.class)
public interface ProjectRolesDAO extends IRolesDAO<ProjectRoleTable> {

    @Override
    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_project_roles (created_at, user_id, role_type, project_id, accepted) " +
        "VALUES (:now, :userId, :roleType, :projectId, :accepted)")
    ProjectRoleTable insert(@BindBean ProjectRoleTable table);

    @Override
    @GetGeneratedKeys
    @SqlUpdate("UPDATE user_project_roles SET role_type = :roleType, accepted = :accepted WHERE id = :id")
    ProjectRoleTable update(@BindBean ProjectRoleTable table);

    @Override
    @SqlUpdate("DELETE FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    void delete(@BindBean ProjectRoleTable table);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE id = :id")
    ProjectRoleTable getTable(long id);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE id = :id AND user_id = :userId")
    ProjectRoleTable getTable(long id, long userId);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND role_type = :role")
    List<ProjectRoleTable> getRoleTablesByPrincipal(long projectId, String role);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    ProjectRoleTable getTableByPrincipal(long projectId, long userId);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    ProjectRoleTable getTable(@BindBean ProjectRoleTable table);
}
