package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.db.mappers.RoleMapperFactory;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(GlobalRoleTable.class)
@RegisterColumnMapperFactory(RoleMapperFactory.class)
public interface GlobalRoleDAO extends RoleDAO<GlobalRoleTable> {

    @SqlUpdate("INSERT INTO user_global_roles VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
    void insert(@BindBean GlobalRoleTable table);

    @Override
    default void update(GlobalRoleTable table) {
        throw new UnsupportedOperationException("Cannot update global roles, just delete them");
    }

    @Override
    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId AND role_id = :roleId")
    void delete(@BindBean GlobalRoleTable table);

    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId")
    void deleteAll(long userId);

    @Override
    default GlobalRoleTable getTable(long id) {
        throw new UnsupportedOperationException("Cannot get global roles by just an id");
    }

    @Override
    @SqlQuery("SELECT * FROM user_global_roles WHERE user_id = :userId AND role_id = :roleId")
    GlobalRoleTable getTable(@BindBean GlobalRoleTable table);
}
