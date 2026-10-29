package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(ProjectRoleTable.class)
public interface ProjectRolesDAO extends IMemberRolesDAO<ProjectRoleTable> {

    // permissions is a bit(64), the column mapper reads a long
    String COLUMNS = "id, created_at, user_id, permissions::bigint AS permissions, title, accepted, is_owner, project_id";

    @Override
    @Timestamped
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO user_project_roles (created_at, user_id, permissions, title, accepted, is_owner, project_id) " +
        "VALUES (:now, :userId, :permissions::bit(64), :title, :accepted, :owner, :projectId)")
    long insert(@BindBean ProjectRoleTable table);

    @Override
    @SqlUpdate("UPDATE user_project_roles SET permissions = :permissions::bit(64), title = :title, accepted = :accepted, is_owner = :owner WHERE id = :id")
    void update(@BindBean ProjectRoleTable table);

    @Override
    @SqlUpdate("DELETE FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    void delete(@BindBean ProjectRoleTable table);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_project_roles WHERE id = :id")
    ProjectRoleTable getTable(long id);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_project_roles WHERE id = :id AND user_id = :userId")
    ProjectRoleTable getTable(long id, long userId);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_project_roles WHERE project_id = :projectId AND is_owner IS TRUE")
    List<ProjectRoleTable> getOwnerTables(long projectId);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    ProjectRoleTable getTableByPrincipal(long projectId, long userId);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_project_roles WHERE project_id = :projectId AND user_id = :userId")
    ProjectRoleTable getTable(@BindBean ProjectRoleTable table);
}
