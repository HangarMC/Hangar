package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.PermissionsDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class PermissionService extends HangarComponent {

    public static final Permission DEFAULT_SIGNED_OUT_PERMISSIONS = Permission.ViewPublicInfo;
    public static final Permission DEFAULT_SIGNED_IN_PERMISSIONS = Permission.ViewPublicInfo.add(Permission.EditOwnUserSettings).add(Permission.EditApiKeys);

    private final PermissionsDAO permissionsDAO;

    public PermissionService(final PermissionsDAO permissionsDAO) {
        this.permissionsDAO = permissionsDAO;
    }

    // Global permissions
    public @NotNull Permission getGlobalPermissions(final @Nullable Long userId) {
        return this.getPermissions(userId, this.permissionsDAO::getGlobalPermission);
    }

    public @NotNull Permission getGlobalPermissions(final @Nullable String userName) {
        return this.getPermissions(userName, this.permissionsDAO::getGlobalPermission);
    }

    // Project permissions
    public Permission getProjectPermissions(final @Nullable Long userId, final long projectId) {
        //TODO still leaks not listed projects (e.g. via pages api) as the perm is technically given
        return this.getPermissions(userId, id -> this.permissionsDAO.getProjectPermission(id, projectId));
    }

    public Permission getProjectPermissions(final @Nullable Long userId, final @NotNull String slug) {
        return this.getPermissions(userId, id -> this.permissionsDAO.getProjectPermission(id, slug));
    }

    public Map<UserTable, Permission> getProjectMemberPermissions(final long projectId) {
        return this.permissionsDAO.getProjectMemberPermissions(projectId);
    }

    // Organization permissions
    public Permission getOrganizationPermissions(final @Nullable Long userId, final long orgId) {
        return this.getPermissions(userId, id -> this.permissionsDAO.getOrganizationPermission(id, orgId));
    }

    public Permission getOrganizationPermissions(final @Nullable Long userId, final @NotNull String orgName) {
        return this.getPermissions(userId, id -> this.permissionsDAO.getOrganizationPermission(id, orgName));
    }

    // Possible permissions for a user
    public Permission getPossibleProjectPermissions(final @Nullable Long userId) {
        return this.getPermissions(userId, this.permissionsDAO::getPossibleProjectPermissions);
    }

    public Permission getPossibleOrganizationPermissions(final @Nullable Long userId) {
        return this.getPermissions(userId, this.permissionsDAO::getPossibleOrganizationPermissions);
    }

    public Permission getAllPossiblePermissions(final @Nullable Long userId) {
        return this.getGlobalPermissions(userId).add(this.getPossibleProjectPermissions(userId)).add(this.getPossibleOrganizationPermissions(userId));
    }

    private @NotNull <T> Permission getPermissions(final @Nullable T identifier, final @NotNull Function<T, Permission> permissionSupplier) {
        if (identifier == null) {
            return DEFAULT_SIGNED_OUT_PERMISSIONS;
        }

        final Permission perm = permissionSupplier.apply(identifier);
        if (perm == null) {
            return DEFAULT_SIGNED_IN_PERMISSIONS;
        }
        return perm.add(DEFAULT_SIGNED_IN_PERMISSIONS);
    }
}
