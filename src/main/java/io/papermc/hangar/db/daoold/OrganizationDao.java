package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@Deprecated(forRemoval = true)
@RegisterBeanMapper(OrganizationsTable.class)
public interface OrganizationDao {

    @SqlQuery("SELECT * FROM organizations WHERE id = :orgId")
    OrganizationsTable getById(long orgId);

    @SqlQuery("SELECT * FROM organizations WHERE user_id = :userId")
    OrganizationsTable getByUserId(long userId);

    @SqlQuery("SELECT * FROM organizations WHERE name = :username")
    OrganizationsTable getByUserName(String username);

    @RegisterBeanMapper(value = OrganizationsTable.class, prefix = "o")
    @RegisterBeanMapper(value = UserOrganizationRolesTable.class, prefix = "r")
    @SqlQuery("SELECT o.id o_id, o.created_at o_created_at, o.name o_name, o.owner_id o_owner_id, o.user_id o_user_id, " +
            "uor.id r_id, uor.created_at r_created_at, uor.user_id r_user_id, uor.role_type r_role_type, uor.organization_id r_organization_id, uor.accepted r_accepted " +
            "FROM organizations o " +
            "   JOIN user_organization_roles uor ON uor.organization_id = o.id " +
            "   JOIN roles r ON uor.role_type = r.name " +
            "WHERE uor.user_id = :userId")
    Map<OrganizationsTable, UserOrganizationRolesTable> getUserOrganizationsAndRoles(long userId);

    @RegisterBeanMapper(value = UserOrganizationRolesTable.class, prefix = "r")
    @RegisterBeanMapper(UsersTable.class)
    @SqlQuery("SELECT u.*, " +
              "uor.id r_id, uor.created_at r_created_at, uor.user_id r_user_id, uor.role_type r_role_type, uor.organization_id r_organization_id, uor.accepted r_accepted " +
              "FROM user_organization_roles uor " +
              "     JOIN users u ON u.id = uor.user_id " +
              "WHERE uor.organization_id = :orgId")
    Map<UserOrganizationRolesTable, UsersTable> getOrgMembers(long orgId);

}
