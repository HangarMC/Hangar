package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;

@Repository
@RegisterBeanMapper(UserOrganizationRolesTable.class)
public interface UserOrganizationRolesDao {

    @RegisterBeanMapper(value = OrganizationsTable.class, prefix = "o")
    @SqlQuery("SELECT uor.*, o.id o_id, o.created_at o_created_at, o.name o_name, o.owner_id o_owner_id, o.user_id o_user_id FROM user_organization_roles uor LEFT OUTER JOIN organizations o ON uor.organization_id = o.id WHERE uor.user_id = :userId AND uor.is_accepted = false ORDER BY uor.created_at")
    Map<UserOrganizationRolesTable, OrganizationsTable> getUnacceptedRoles(long userId);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO user_organization_roles (created_at, user_id, role_type, organization_id, is_accepted) VALUES (:now, :userId, :roleType, :organizationId, :isAccepted)")
    UserOrganizationRolesTable insert(@BindBean UserOrganizationRolesTable userOrganizationRolesTable);

    @SqlQuery("SELECT uor.* FROM user_organization_roles uor " +
            "JOIN roles r ON uor.role_type = r.name " +
            "WHERE r.category = 'organization' AND uor.organization_id = :orgId AND uor.user_id = :userId " +
            "ORDER BY r.id DESC") // should get roles in correct hierarchy order, might be useful somewhere
    List<UserOrganizationRolesTable> getUserRoles(long orgId, long userId);
}
