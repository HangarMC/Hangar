package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface OrganizationRoleDAO extends RoleDAO<OrganizationRoleTable> {

    @Override
    @Timestamped
    @SqlUpdate("INSERT INTO user_organization_roles (created_at, user_id, role_type, organization_id, is_accepted) " +
               "VALUES (:now, :userId, :roleType, :organizationId, :isAccepted)")
    void insert(@BindBean OrganizationRoleTable table);

    @Override
    @SqlUpdate("UPDATE user_organization_roles SET role_type = :roleType, is_accepted = :isAccepted WHERE id = :id")
    void update(@BindBean OrganizationRoleTable table);

    @Override
    @SqlUpdate("DELETE FROM user_organization_roles WHERE organization_id = :organizationId AND user_id = :userId")
    void delete(@BindBean OrganizationRoleTable table);

    @Override
    @SqlQuery("SELECT * FROM user_organization_roles WHERE id = :id")
    OrganizationRoleTable getTable(long id);

    @Override
    @SqlQuery("SELECT * FROM user_organization_roles WHERE organization_id = :organizationId AND user_id = :userId")
    OrganizationRoleTable getTable(@BindBean OrganizationRoleTable table);
}
