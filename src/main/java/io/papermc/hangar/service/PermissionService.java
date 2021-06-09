package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.PermissionsDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Service
public class PermissionService extends HangarComponent {

    public static final Permission DEFAULT_SIGNED_OUT_PERMISSIONS = Permission.ViewPublicInfo;
    public static final Permission DEFAULT_SIGNED_IN_PERMISSIONS = Permission.ViewPublicInfo.add(Permission.EditOwnUserSettings).add(Permission.EditApiKeys);

    private final PermissionsDAO permissionsDAO;

    public PermissionService(PermissionsDAO permissionsDAO) {
        this.permissionsDAO = permissionsDAO;
    }

    @NotNull
    // Global permissions
    public Permission getGlobalPermissions(@Nullable Long userId) {
        return getPermissions(userId, permissionsDAO::getGlobalPermission);
    }

    @NotNull
    public Permission getGlobalPermissions(@Nullable String userName) {
        return getPermissions(userName, permissionsDAO::getGlobalPermission);
    }

    // Project permissions
    public Permission getProjectPermissions(@Nullable Long userId, long projectId) {
        return getPermissions(userId, id -> permissionsDAO.getProjectPermission(id, projectId));
    }

    public Permission getProjectPermissions(@Nullable Long userId, @NotNull String author, @NotNull String slug) {
        return getPermissions(userId, id -> permissionsDAO.getProjectPermission(id, author, slug));
    }

    public Map<UserTable, Permission> getProjectMemberPermissions(long projectId) {
        return permissionsDAO.getProjectMemberPermissions(projectId);
    }

    // Organization permissions
    public Permission getOrganizationPermissions(@Nullable Long userId, long orgId) {
        return getPermissions(userId, id -> permissionsDAO.getOrganizationPermission(id, orgId));
    }

    public Permission getOrganizationPermissions(@Nullable Long userId, @NotNull String orgName) {
        return getPermissions(userId, id -> permissionsDAO.getOrganizationPermission(id, orgName));
    }

    // Possible permissions for a user
    public Permission getPossibleProjectPermissions(@Nullable Long userId) {
        return getPermissions(userId, permissionsDAO::getPossibleProjectPermissions);
    }

    public Permission getPossibleOrganizationPermissions(@Nullable Long userId) {
        return getPermissions(userId, permissionsDAO::getPossibleOrganizationPermissions);
    }

    public Permission getAllPossiblePermissions(@Nullable Long userId) {
        return getGlobalPermissions(userId).add(getPossibleProjectPermissions(userId)).add(getPossibleOrganizationPermissions(userId));
    }

    @NotNull
    private <T> Permission getPermissions(@Nullable T identifier, @NotNull Function<T, Permission> permissionSupplier) {
        if (identifier == null) {
            return DEFAULT_SIGNED_OUT_PERMISSIONS;
        }
        Permission perm = permissionSupplier.apply(identifier);
        if (perm == null) {
            return DEFAULT_SIGNED_IN_PERMISSIONS;
        }
        else return perm.add(DEFAULT_SIGNED_IN_PERMISSIONS);
    }
}
