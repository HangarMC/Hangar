package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.model.Permission;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(value = Permission.class, prefix = "perm")
public interface PermissionsDao {

    @SqlQuery("SELECT gt.permission::BIGINT perm_value FROM global_trust gt JOIN users u on gt.user_id = u.id WHERE (u.id = :userId OR u.name = :userName)")
    Permission getGlobalPermission(Long userId, String userName);

    @SqlQuery("SELECT permission::BIGINT FROM project_trust pt JOIN users u ON pt.user_id = u.id WHERE pt.user_id = :userId AND pt.project_id = :projectId")
    Permission getProjectPermission(long userId, long projectId);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_project_roles upr JOIN roles r ON upr.role_type = r.name WHERE upr.user_id = :userId")
    Permission getPossibleProjectPermissions(long userId);

    @SqlQuery("SELECT coalesce(bit_or(r.permission), B'0'::BIT(64))::BIGINT perm_value FROM user_organization_roles uor JOIN roles r ON uor.role_type = r.name WHERE uor.user_id = :userId")
    Permission getPossibleOrganizationPermissions(long userId);
}
