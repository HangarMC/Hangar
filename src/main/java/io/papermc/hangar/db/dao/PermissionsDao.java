package io.papermc.hangar.db.dao;

import io.papermc.hangar.model.Permission;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(value = Permission.class, prefix = "perm")
public interface PermissionsDao {

    @SqlQuery("SELECT coalesce(gt.permission, B'0'::BIT(64))::BIGINT perm_value FROM global_trust gt JOIN users u on gt.user_id = u.id WHERE (u.id = :userId OR u.name = :userName)")
    Permission getGlobalPermission(Long userId, String userName);

    @SqlQuery("SELECT coalesce(permission, B'0'::BIT(64))::BIGINT perm_value FROM project_trust pt JOIN users u ON pt.user_id = u.id WHERE pt.user_id = :userId AND pt.project_id = :projectId")
    Permission getProjectPermission(long userId, long projectId);

    @SqlQuery("SELECT coalesce(permission, B'0'::BIT(64))::BIGINT perm_value FROM project_trust pt JOIN users u ON pt.user_id = u.id JOIN projects p ON pt.project_id = p.id WHERE pt.user_id = :userId AND p.plugin_id = :pluginId")
    Permission getProjectPermission(long userId, String pluginId);

    @SqlQuery("SELECT coalesce(permission, B'0'::BIT(64))::BIGINT perm_value FROM organization_trust ot JOIN organizations o ON ot.organization_id = o.id WHERE ot.user_id = :userId AND o.name = :orgName")
    Permission getOrgPermission(long userId, String orgName);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_project_roles upr JOIN roles r ON upr.role_type = r.name WHERE upr.user_id = :userId")
    Permission getPossibleProjectPermissions(long userId);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_organization_roles uor JOIN roles r ON uor.role_type = r.name WHERE uor.user_id = :userId")
    Permission getPossibleOrganizationPermissions(long userId);
}
