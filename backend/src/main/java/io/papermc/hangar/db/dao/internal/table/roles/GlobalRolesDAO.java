package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.db.mappers.factories.RoleColumnMapperFactory;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(GlobalRoleTable.class)
@RegisterColumnMapperFactory(RoleColumnMapperFactory.class)
public interface GlobalRolesDAO extends IRolesDAO<GlobalRoleTable> {

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_global_roles VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
    GlobalRoleTable insert(@BindBean GlobalRoleTable table);

    @Override
    default GlobalRoleTable update(final GlobalRoleTable table) {
        throw new UnsupportedOperationException("Cannot update global roles, just delete them");
    }

    @Override
    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId AND role_id = :roleId")
    void delete(@BindBean GlobalRoleTable table);

    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId")
    void deleteAll(long userId);

    @Override
    default GlobalRoleTable getTable(final long id) {
        throw new UnsupportedOperationException("Cannot get global roles by just an id");
    }

    @Override
    default GlobalRoleTable getTable(final long id, final long userId) {
        throw new UnsupportedOperationException("Cannot get global roles by just an id");
    }

    @Override
    default List<GlobalRoleTable> getRoleTablesByPrincipal(final long principalId, final String role) {
        throw new UnsupportedOperationException("Cannot get global roles with a principal id");
    }

    @Override
    default GlobalRoleTable getTableByPrincipal(final long principalId, final long userId) {
        throw new UnsupportedOperationException("Cannot get global roles with a principal id");
    }

    @Override
    @SqlQuery("SELECT * FROM user_global_roles WHERE user_id = :userId AND role_id = :roleId")
    GlobalRoleTable getTable(@BindBean GlobalRoleTable table);

    @SqlQuery("SELECT ugr.* " +
        "   FROM user_global_roles ugr" +
        "   JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE ugr.user_id = :userId" +
        "   ORDER BY r.permission::bigint DESC")
    List<GlobalRoleTable> getGlobalRoleTables(long userId);
}
