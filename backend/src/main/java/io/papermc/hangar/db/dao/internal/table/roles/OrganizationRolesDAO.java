package io.papermc.hangar.db.dao.internal.table.roles;

import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(OrganizationRoleTable.class)
public interface OrganizationRolesDAO extends IMemberRolesDAO<OrganizationRoleTable> {

    // permissions is a bit(64), the column mapper reads a long
    String COLUMNS = "uor.id, uor.created_at, uor.user_id, uor.permissions::bigint AS permissions, uor.title, uor.accepted, uor.is_owner, uor.organization_id";

    @Override
    @Timestamped
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO user_organization_roles (created_at, user_id, permissions, title, accepted, is_owner, organization_id) " +
        "VALUES (:now, :userId, :permissions::bit(64), :title, :accepted, :owner, :organizationId)")
    long insert(@BindBean OrganizationRoleTable table);

    @Override
    @SqlUpdate("UPDATE user_organization_roles SET permissions = :permissions::bit(64), title = :title, accepted = :accepted, is_owner = :owner WHERE id = :id")
    void update(@BindBean OrganizationRoleTable table);

    @Override
    @SqlUpdate("DELETE FROM user_organization_roles WHERE organization_id = :organizationId AND user_id = :userId")
    void delete(@BindBean OrganizationRoleTable table);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_organization_roles uor WHERE uor.id = :id")
    OrganizationRoleTable getTable(long id);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_organization_roles uor WHERE uor.id = :id AND uor.user_id = :userId")
    OrganizationRoleTable getTable(long id, long userId);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_organization_roles uor WHERE uor.organization_id = :organizationId AND uor.is_owner IS TRUE")
    List<OrganizationRoleTable> getOwnerTables(long organizationId);

    @Override
    @SqlQuery("SELECT " + COLUMNS + " FROM user_organization_roles uor WHERE uor.organization_id = :organizationId AND uor.user_id = :userId")
    OrganizationRoleTable getTableByPrincipal(long organizationId, long userId);

    @Override
    @SqlQuery("SELECT " + COLUMNS + ", ow.id AS ownerId, ow.name AS ownerName FROM user_organization_roles uor " +
        "  JOIN organizations o ON o.id = uor.organization_id" +
        "  JOIN users ow ON o.owner_id = ow.id " +
        "WHERE uor.organization_id = :organizationId AND uor.user_id = :userId")
    OrganizationRoleTable getTable(@BindBean OrganizationRoleTable table);

    @KeyColumn("name")
    @SqlQuery("""
        SELECT o.name, uor.id, uor.created_at, uor.user_id, uor.permissions::bigint AS permissions, uor.title, uor.accepted, uor.is_owner, uor.organization_id,
               ow.id AS ownerId, ow.name AS ownerName, ou.uuid, ou.avatar_url
           FROM user_organization_roles uor
               JOIN organizations o ON o.id = uor.organization_id
               JOIN users u ON uor.user_id = u.id
               JOIN users ou ON ou.id = o.user_id
               JOIN users ow ON o.owner_id = ow.id
           WHERE lower(u.name) = lower(:user) AND uor.accepted IS TRUE
        """)
    Map<String, OrganizationRoleTable> getUserOrganizationRoles(String user, Long userId);
}
