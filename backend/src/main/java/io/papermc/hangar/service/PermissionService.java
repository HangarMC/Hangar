package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.PermissionsDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.security.authentication.api.HangarApiPrincipal;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
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
    public Permission getGlobalPermissions(final @Nullable Long userId) {
        return this.getPermissions(userId, this.permissionsDAO::getGlobalPermission);
    }

    // Project permissions
    public Permission getProjectPermissions(final @Nullable Long userId, final long projectId) {
        //TODO still leaks not listed projects (e.g. via pages api) as the perm is technically given
        if (this.outOfKeyScope(key -> key.coversProject(projectId))) {
            return DEFAULT_SIGNED_OUT_PERMISSIONS;
        }
        return this.getPermissions(userId, id -> this.permissionsDAO.getProjectPermission(id, projectId));
    }

    public Permission getProjectPermissions(final @Nullable Long userId, final String slug) {
        if (this.outOfKeyScope(key -> key.coversProject(slug))) {
            return DEFAULT_SIGNED_OUT_PERMISSIONS;
        }
        return this.getPermissions(userId, id -> this.permissionsDAO.getProjectPermission(id, slug));
    }

    // A project scoped api key acts like a signed out visitor on every project it wasn't created for
    private boolean outOfKeyScope(final Predicate<ApiKeyTable> covered) {
        return this.getOptionalHangarPrincipal()
            .filter(HangarApiPrincipal.class::isInstance)
            .map(principal -> ((HangarApiPrincipal) principal).getApiKeyTable())
            .map(key -> !covered.test(key))
            .orElse(false);
    }

    public Map<UserTable, Permission> getProjectMemberPermissions(final long projectId) {
        return this.permissionsDAO.getProjectMemberPermissions(projectId);
    }

    // Organization permissions
    public Permission getOrganizationPermissions(final @Nullable Long userId, final long orgId) {
        return this.getPermissions(userId, id -> this.permissionsDAO.getOrganizationPermission(id, orgId));
    }

    public Permission getOrganizationPermissions(final @Nullable Long userId, final String orgName) {
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

    private <T> Permission getPermissions(final @Nullable T identifier, final Function<T, Permission> permissionSupplier) {
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
