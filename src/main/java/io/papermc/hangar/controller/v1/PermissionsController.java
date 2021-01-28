package io.papermc.hangar.controller.v1;

import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.controller.v1.interfaces.IPermissionsController;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.PermissionType;
import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.service.PermissionService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.BiPredicate;

public class PermissionsController implements IPermissionsController {

    private final PermissionService permissionService;

    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public PermissionsController(PermissionService permissionService, ApiAuthInfo apiAuthInfo) {
        this.permissionService = permissionService;
        this.apiAuthInfo = apiAuthInfo;
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAllPermissions(List<NamedPermission> permissions, String author, String slug, String organization) {
        return has(permissions, author, slug, organization, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAny(List<NamedPermission> permissions, String author, String slug, String organization) {
        return has(permissions, author, slug, organization, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<UserPermissions> showPermissions(String author, String slug, String organization) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(author, slug, organization);
        return ResponseEntity.ok(new UserPermissions(scopedPerms.getLeft(), scopedPerms.getRight().toNamed()));
    }

    private Pair<PermissionType, Permission> getPermissionsInScope(String author, String slug, String organization) {
        if (author != null && slug != null && organization == null) { // author & slug
            Permission perms = permissionService.getProjectPermissions(apiAuthInfo.getUserId(), author, slug);
            return new ImmutablePair<>(PermissionType.PROJECT, perms);
        } else if (author != null && slug == null && organization == null) { // just author
            Permission perms = permissionService.getGlobalPermissions(author);
            return new ImmutablePair<>(PermissionType.GLOBAL, perms);
        } else if (author == null && slug == null && organization != null) { // just org
            Permission perms = permissionService.getOrganizationPermissions(apiAuthInfo.getUserId(), organization);
            return new ImmutablePair<>(PermissionType.ORGANIZATION, perms);
        } else {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Incorrect request parameters");
        }
    }

    private ResponseEntity<PermissionCheck> has(List<NamedPermission> perms, String author, String slug, String organization, BiPredicate<List<NamedPermission>, Permission> check) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(author, slug, organization);
        return ResponseEntity.ok(new PermissionCheck(scopedPerms.getLeft(), check.test(perms, scopedPerms.getRight())));
    }
}
