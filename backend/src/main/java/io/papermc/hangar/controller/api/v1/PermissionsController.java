package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IPermissionsController;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.permissions.PermissionCheck;
import io.papermc.hangar.model.api.permissions.UserPermissions;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.PermissionService;
import java.util.Collection;
import java.util.Set;
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
    public ResponseEntity<PermissionCheck> hasAllPermissions(final Set<NamedPermission> permissions, final ProjectTable project, final OrganizationTable organization) {
        return this.has(permissions, project, organization, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAllPermissionsLegacy(final Set<NamedPermission> permissions, final ProjectTable slug, final OrganizationTable organization) {
        return this.has(permissions, slug, organization, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAny(final Set<NamedPermission> permissions, final ProjectTable project, final OrganizationTable organization) {
        return this.has(permissions, project, organization, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAnyLegacy(final Set<NamedPermission> permissions, final ProjectTable slug, final OrganizationTable organization) {
        return this.has(permissions, slug, organization, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<UserPermissions> showPermissions(final ProjectTable project, final OrganizationTable organization) {
        final Pair<PermissionType, Permission> scopedPerms = this.getPermissionsInScope(project, organization);
        return ResponseEntity.ok(new UserPermissions(scopedPerms.getLeft(), scopedPerms.getRight().toBinString(), scopedPerms.getRight().toNamed()));
    }

    @Override
    public ResponseEntity<UserPermissions> showPermissionsLegacy(final ProjectTable slug, final OrganizationTable organization) {
        final Pair<PermissionType, Permission> scopedPerms = this.getPermissionsInScope(slug, organization);
        return ResponseEntity.ok(new UserPermissions(scopedPerms.getLeft(), scopedPerms.getRight().toBinString(), scopedPerms.getRight().toNamed()));
    }

    private Pair<PermissionType, Permission> getPermissionsInScope(final ProjectTable project, final OrganizationTable organization) {
        if (project != null && organization != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Specifying both project and organization is not allowed");
        }

        if (project != null) {
            final Permission perms = this.permissionService.getProjectPermissions(this.getHangarUserId(), project.getId());
            return new ImmutablePair<>(PermissionType.PROJECT, perms);
        }

        if (organization != null) {
            final Permission perms = this.permissionService.getOrganizationPermissions(this.getHangarUserId(), organization.getId());
            return new ImmutablePair<>(PermissionType.ORGANIZATION, perms);
        }

        Permission perms = this.permissionService.getGlobalPermissions(this.getHangarUserId());
        perms = this.getHangarPrincipal().getPossiblePermissions().intersect(perms);
        return new ImmutablePair<>(PermissionType.GLOBAL, perms);
    }

    private ResponseEntity<PermissionCheck> has(final Collection<NamedPermission> perms, final ProjectTable project, final OrganizationTable organization, final BiPredicate<Collection<NamedPermission>, Permission> check) {
        final Pair<PermissionType, Permission> scopedPerms = this.getPermissionsInScope(project, organization);
        return ResponseEntity.ok(new PermissionCheck(scopedPerms.getLeft(), check.test(perms, scopedPerms.getRight())));
    }
}
