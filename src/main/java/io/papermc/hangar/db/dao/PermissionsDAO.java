package io.papermc.hangar.db.dao;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RegisterBeanMapper(value = Permission.class, prefix = "perm")
public interface PermissionsDAO {

    @SqlQuery("SELECT coalesce(gt.permission, B'0'::BIT(64))::BIGINT perm_value" +
              " FROM users u " +
              "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
              " WHERE u.id = :userId OR u.name = :userName")
    Permission _getGlobalPermission(Long userId, String userName);
    default Permission getGlobalPermission(long userId) {
        return _getGlobalPermission(userId, null);
    }
    default Permission getGlobalPermission(@NotNull String userName) {
        return _getGlobalPermission(null, userName);
    }

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(pt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
            " FROM users u " +
            "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
            "     LEFT JOIN projects p ON (lower(p.owner_name) = lower(:author) AND p.slug = :slug) OR p.id = :projectId" +
            "     LEFT JOIN project_trust pt ON u.id = pt.user_id AND pt.project_id = p.id" +
            "     LEFT JOIN organization_trust ot ON u.id = ot.user_id AND ot.organization_id = p.owner_id" +
            " WHERE u.id = :userId")
    Permission _getProjectPermission(long userId, Long projectId, String author, String slug);
    default Permission getProjectPermission(long userId, long projectId) {
        return _getProjectPermission(userId, projectId, null, null);
    }
    default Permission getProjectPermission(long userId, String author, String slug) {
        return _getProjectPermission(userId, null, author, slug);
    }

    @ValueColumn("permission")
    @RegisterConstructorMapper(UserTable.class)
    @SqlQuery("SELECT u.*, (coalesce(gt.permission, B'0'::bit(64)) | coalesce(pt.permission, B'0'::bit(64)) | coalesce(ot.permission, B'0'::bit(64)))::bigint AS permission" +
            "   FROM users u" +
            "       JOIN project_trust pt ON u.id = pt.user_id" +
            "       JOIN projects p ON pt.project_id = p.id" +
            "       LEFT JOIN global_trust gt ON u.id = gt.user_id" +
            "       LEFT JOIN organization_trust ot ON u.id = ot.user_id AND ot.organization_id = p.owner_id" +
            "   WHERE pt.project_id = :projectId")
    Map<UserTable, Permission> getProjectMemberPermissions(long projectId);

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
              " FROM users u " +
              "     LEFT JOIN organizations o ON o.name = :orgName OR o.id = :orgId" +
              "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
              "     LEFT JOIN organization_trust ot ON o.id = ot.organization_id AND ot.user_id = u.id" +
              " WHERE u.id = :userId")
    Permission _getOrganizationPermission(long userId, String orgName, Long orgId);
    default Permission getOrganizationPermission(long userId, String orgName) {
        return _getOrganizationPermission(userId, orgName, null);
    }
    default Permission getOrganizationPermission(long userId, long orgId) {
        return _getOrganizationPermission(userId, null, orgId);
    }

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_project_roles upr JOIN roles r ON upr.role_type = r.name WHERE upr.user_id = :userId")
    Permission getPossibleProjectPermissions(long userId);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_organization_roles uor JOIN roles r ON uor.role_type = r.name WHERE uor.user_id = :userId")
    Permission getPossibleOrganizationPermissions(long userId);
}
