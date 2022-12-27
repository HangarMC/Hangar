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
import java.util.List;
import java.util.function.BiPredicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Anyone
@Controller
@RateLimit(path = "apipermissions", refillTokens = 100, greedy = true)
public class PermissionsController extends HangarComponent implements IPermissionsController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionsController(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAllPermissions(final List<NamedPermission> permissions, final String author, final String slug, final String organization) {
        return this.has(permissions, author, slug, organization, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAny(final List<NamedPermission> permissions, final String author, final String slug, final String organization) {
        return this.has(permissions, author, slug, organization, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<UserPermissions> showPermissions(final String author, final String slug, final String organization) {
        final Pair<PermissionType, Permission> scopedPerms = this.getPermissionsInScope(author, slug, organization);
        return ResponseEntity.ok(new UserPermissions(scopedPerms.getLeft(), scopedPerms.getRight().toBinString(), scopedPerms.getRight().toNamed()));
    }

    private Pair<PermissionType, Permission> getPermissionsInScope(final String author, final String slug, final String organization) {
        if (author != null && slug != null && organization == null) { // author & slug
            Permission perms = this.permissionService.getProjectPermissions(this.getHangarUserId(), author, slug);
            perms = this.getHangarPrincipal().getPossiblePermissions().intersect(perms);
            return new ImmutablePair<>(PermissionType.PROJECT, perms);
        } else if (author == null && slug == null && organization == null) { // current user (I don't think there's a need to see other user's global permissions)
            Permission perms = this.permissionService.getGlobalPermissions(this.getHangarUserId());
            perms = this.getHangarPrincipal().getPossiblePermissions().intersect(perms);
            return new ImmutablePair<>(PermissionType.GLOBAL, perms);
        } else if (author == null && slug == null) { // just org
            Permission perms = this.permissionService.getOrganizationPermissions(this.getHangarUserId(), organization);
            perms = this.getHangarPrincipal().getPossiblePermissions().intersect(perms);
            return new ImmutablePair<>(PermissionType.ORGANIZATION, perms);
        } else {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Incorrect request parameters");
        }
    }

    private ResponseEntity<PermissionCheck> has(final List<NamedPermission> perms, final String author, final String slug, final String organization, final BiPredicate<List<NamedPermission>, Permission> check) {
        final Pair<PermissionType, Permission> scopedPerms = this.getPermissionsInScope(author, slug, organization);
        return ResponseEntity.ok(new PermissionCheck(scopedPerms.getLeft(), check.test(perms, scopedPerms.getRight())));
    }
}
