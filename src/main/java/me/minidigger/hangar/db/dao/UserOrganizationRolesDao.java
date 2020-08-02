package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(UserOrganizationRolesTable.class)
public interface UserOrganizationRolesDao {

    @RegisterBeanMapper(value = OrganizationsTable.class, prefix = "o")
    @SqlQuery("SELECT uor.*, o.id o_id, o.created_at o_created_at, o.name o_name, o.owner_id o_owner_id, o.user_id o_user_id FROM user_organization_roles uor LEFT OUTER JOIN organizations o ON uor.organization_id = o.id WHERE uor.user_id = :userId AND uor.is_accepted = false ORDER BY uor.created_at")
    Map<UserOrganizationRolesTable, OrganizationsTable> getUnacceptedRoles(long userId);
}
