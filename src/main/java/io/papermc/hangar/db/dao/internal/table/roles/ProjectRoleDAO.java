package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.db.mappers.RoleMapperFactory;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectRoleTable.class)
@RegisterColumnMapperFactory(RoleMapperFactory.class)
public interface ProjectRoleDAO extends RoleDAO<ProjectRoleTable> {

    @Override
    @Timestamped
    @SqlUpdate("INSERT INTO user_project_roles (created_at, user_id, role_type, project_id, is_accepted) " +
               "VALUES (:now, :userId, :roleType, :projectId, :isAccepted)")
    void insert(@BindBean ProjectRoleTable table);

    @Override
    @SqlUpdate("UPDATE user_project_roles SET role_type = :roleType, is_accepted = :isAccepted WHERE id = :id")
    void update(@BindBean ProjectRoleTable table);

    @Override
    @SqlUpdate("DELETE FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    void delete(@BindBean ProjectRoleTable table);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE id = :id")
    ProjectRoleTable getTable(long id);

    @Override
    @SqlQuery("SELECT * FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    ProjectRoleTable getTable(@BindBean ProjectRoleTable table);
}
