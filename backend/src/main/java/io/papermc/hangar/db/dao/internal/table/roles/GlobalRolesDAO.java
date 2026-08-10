package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.db.mappers.factories.RoleColumnMapperFactory;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(GlobalRoleTable.class)
@RegisterColumnMapperFactory(RoleColumnMapperFactory.class)
public interface GlobalRolesDAO {

    @SqlUpdate("INSERT INTO user_global_roles VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
    void insert(@BindBean GlobalRoleTable table);

    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId AND role_id = :roleId")
    void delete(@BindBean GlobalRoleTable table);

    @SqlUpdate("DELETE FROM user_global_roles WHERE user_id = :userId")
    void deleteAll(long userId);

    @SqlQuery("SELECT ugr.* " +
        "   FROM user_global_roles ugr" +
        "   JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE ugr.user_id = :userId" +
        "   ORDER BY r.permission::bigint DESC")
    List<GlobalRoleTable> getGlobalRoleTables(long userId);
}
