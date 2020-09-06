package io.papermc.hangar.db.dao;

import io.papermc.hangar.model.Permission;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(value = Permission.class, prefix = "perm")
public interface PermissionsDao {

    @SqlQuery("SELECT coalesce(gt.permission, B'0'::BIT(64))::BIGINT perm_value" +
              " FROM users u " +
              "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
              " WHERE u.id = :userId OR u.name = :userName")
    Permission getGlobalPermission(Long userId, String userName);

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(pt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
              " FROM users u " +
              "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
              "     LEFT JOIN projects p ON p.id = :projectId" +
              "     LEFT JOIN project_trust pt ON u.id = pt.user_id AND pt.project_id = p.id" +
              "     LEFT JOIN organization_trust ot ON u.id = ot.user_id AND ot.organization_id = p.owner_id" +
              " WHERE u.id = :userId")
    Permission getProjectPermission(long userId, long projectId);

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(pt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
            " FROM users u " +
            "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
            "     LEFT JOIN projects p ON p.plugin_id = :pluginId" +
            "     LEFT JOIN project_trust pt ON u.id = pt.user_id AND pt.project_id = p.id" +
            "     LEFT JOIN organization_trust ot ON u.id = ot.user_id AND ot.organization_id = p.owner_id" +
            " WHERE u.id = :userId")
    Permission getProjectPermission(long userId, String pluginId);

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(pt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
            " FROM users u " +
            "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
            "     LEFT JOIN projects p ON lower(p.owner_name) = lower(:author) AND p.slug = :slug" +
            "     LEFT JOIN project_trust pt ON u.id = pt.user_id AND pt.project_id = p.id" +
            "     LEFT JOIN organization_trust ot ON u.id = ot.user_id AND ot.organization_id = p.owner_id" +
            " WHERE u.id = :userId")
    Permission getProjectPermission(long userId, String author, String slug);

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
              " FROM users u " +
              "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
              "     LEFT JOIN organization_trust ot ON u.id = ot.user_id AND ot.organization_id = :orgId" +
              " WHERE u.id = :userId")
    Permission getOrgPermission(long userId, long orgId);

    @SqlQuery("SELECT (coalesce(gt.permission, B'0'::BIT(64)) | coalesce(ot.permission, B'0'::BIT(64)))::BIGINT AS perm_value" +
              " FROM users u " +
              "     LEFT JOIN organizations o ON o.name = :orgName" +
              "     LEFT JOIN global_trust gt ON u.id = gt.user_id" +
              "     LEFT JOIN organization_trust ot ON o.id = ot.organization_id AND ot.user_id = u.id" +
              " WHERE u.id = :userId")
    Permission getOrgPermission(long userId, String orgName);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_project_roles upr JOIN roles r ON upr.role_type = r.name WHERE upr.user_id = :userId")
    Permission getPossibleProjectPermissions(long userId);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_organization_roles uor JOIN roles r ON uor.role_type = r.name WHERE uor.user_id = :userId")
    Permission getPossibleOrganizationPermissions(long userId);
}
