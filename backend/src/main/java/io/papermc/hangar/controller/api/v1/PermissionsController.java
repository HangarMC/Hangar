package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IPermissionsController;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.PermissionService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.function.BiPredicate;

@Anyone
@Controller
@RateLimit(path = "apipermissions", refillTokens = 100, greedy = true)
public class PermissionsController extends HangarComponent implements IPermissionsController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionsController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAllPermissions(List<NamedPermission> permissions, String author, String slug, String organization) {
        return has(permissions, author, slug, organization, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAny(List<NamedPermission> permissions, String author, String slug, String organization) {
        return has(permissions, author, slug, organization, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<UserPermissions> showPermissions(String author, String slug, String organization) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(author, slug, organization);
        return ResponseEntity.ok(new UserPermissions(scopedPerms.getLeft(), scopedPerms.getRight().toBinString(), scopedPerms.getRight().toNamed()));
    }

    private Pair<PermissionType, Permission> getPermissionsInScope(String author, String slug, String organization) {
        if (author != null && slug != null && organization == null) { // author & slug
            Permission perms = permissionService.getProjectPermissions(getHangarUserId(), author, slug);
            perms = getHangarPrincipal().getPossiblePermissions().intersect(perms);
            return new ImmutablePair<>(PermissionType.PROJECT, perms);
        } else if (author == null && slug == null && organization == null) { // current user (I don't think there's a need to see other user's global permissions)
            Permission perms = permissionService.getGlobalPermissions(getHangarUserId());
            perms = getHangarPrincipal().getPossiblePermissions().intersect(perms);
            return new ImmutablePair<>(PermissionType.GLOBAL, perms);
        } else if (author == null && slug == null) { // just org
            Permission perms = permissionService.getOrganizationPermissions(getHangarUserId(), organization);
            perms = getHangarPrincipal().getPossiblePermissions().intersect(perms);
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
