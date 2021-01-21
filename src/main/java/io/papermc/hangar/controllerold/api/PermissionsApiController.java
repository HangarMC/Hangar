package io.papermc.hangar.controllerold.api;

import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.modelold.PermissionType;
import io.papermc.hangar.modelold.generated.PermissionCheck;
import io.papermc.hangar.modelold.generated.Permissions;
import io.papermc.hangar.service.PermissionService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.function.BiPredicate;

@Controller
public class PermissionsApiController implements PermissionsApi {

    private final PermissionService permissionService;
    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public PermissionsApiController(PermissionService permissionService, ApiAuthInfo apiAuthInfo) {
        this.permissionService = permissionService;
        this.apiAuthInfo = apiAuthInfo;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).None, T(io.papermc.hangar.controller.ApiScope).forGlobal())")
    public ResponseEntity<PermissionCheck> hasAll(List<NamedPermission> permissions, String author, String slug) {
        return has(permissions, author, slug, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAny(List<NamedPermission> permissions, String author, String slug) {
        return has(permissions, author, slug, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).None, T(io.papermc.hangar.controller.ApiScope).forGlobal())")
    public ResponseEntity<Permissions> showPermissions(String author, String slug) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(author, slug);
        return ResponseEntity.ok(new Permissions().type(scopedPerms.getLeft()).permissions(scopedPerms.getRight().toNamed()));
    }

    private Pair<PermissionType, Permission> getPermissionsInScope(String author, String slug) {
        if (slug != null) {
            Permission perms = permissionService.getProjectPermissions(apiAuthInfo.getUser(), author, slug);
            return new ImmutablePair<>(PermissionType.PROJECT, perms);
        } else {
            Permission perms = permissionService.getOrganizationPermissions(apiAuthInfo.getUser(), author);
            return new ImmutablePair<>(PermissionType.ORGANIZATION, perms);
        }
    }

    private ResponseEntity<PermissionCheck> has(List<NamedPermission> perms, String author, String slug, BiPredicate<List<NamedPermission>, Permission> check) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(author, slug);
        return ResponseEntity.ok(new PermissionCheck().type(scopedPerms.getLeft()).result(check.test(perms, scopedPerms.getRight())));
    }
}

