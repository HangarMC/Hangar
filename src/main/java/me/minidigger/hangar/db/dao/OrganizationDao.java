package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(OrganizationsTable.class)
public interface OrganizationDao {

    @SqlUpdate("insert into organizations (created_at, name, owner_id, user_id) values (:now, :name, :ownerId, :userId)")
    @Timestamped
    @GetGeneratedKeys
    OrganizationsTable insert(@BindBean OrganizationsTable organization);

    @SqlQuery("SELECT * FROM organizations WHERE id = :orgId")
    OrganizationsTable getById(long orgId);

    @SqlQuery("SELECT o.id, o.created_at, o.name, o.owner_id, o.user_id FROM organization_members om JOIN organizations o ON om.organization_id = o.id WHERE om.user_id = :id")
    List<OrganizationsTable> getUserOrgs(long id);

    @RegisterBeanMapper(value = OrganizationsTable.class, prefix = "o")
    @RegisterBeanMapper(value = UserOrganizationRolesTable.class, prefix = "r")
    @SqlQuery("SELECT o.id o_id, o.created_at o_created_at, o.name o_name, o.owner_id o_owner_id, o.user_id o_user_id, " +
            "uor.id r_id, uor.created_at r_created_at, uor.user_id r_user_id, uor.role_type r_role_type, uor.organization_id r_organization_id, uor.is_accepted r_is_accepted " +
            "FROM organizations o " +
            "   JOIN user_organization_roles uor ON uor.organization_id = o.id " +
            "   JOIN roles r ON uor.role_type = r.name " +
            "WHERE uor.user_id = :userId")
    Map<OrganizationsTable, UserOrganizationRolesTable> getUserOrganizationsAndRoles(long userId);

    @SqlQuery("SELECT * FROM organizations WHERE owner_id = :userId")
    List<OrganizationsTable> getUserOwnedOrgs(long userId);
}
