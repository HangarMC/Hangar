package me.minidigger.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.minidigger.hangar.model.ApiAuthInfo;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.PermissionType;
import me.minidigger.hangar.model.generated.PermissionCheck;
import me.minidigger.hangar.model.generated.Permissions;
import me.minidigger.hangar.service.PermissionService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.BiPredicate;

@Controller
public class PermissionsApiController implements PermissionsApi {

    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    private final ApiAuthInfo apiAuthInfo;
    private final HttpServletRequest request;

    @Autowired
    public PermissionsApiController(PermissionService permissionService, ObjectMapper objectMapper, ApiAuthInfo apiAuthInfo, HttpServletRequest request) {
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
        this.apiAuthInfo = apiAuthInfo;
        this.request = request;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(me.minidigger.hangar.model.Permission).None, T(me.minidigger.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<PermissionCheck> hasAll(List<NamedPermission> permissions, String pluginId, String organizationName) {
       return has(permissions, pluginId, organizationName, (namedPerms, perm) -> namedPerms.stream().anyMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    public ResponseEntity<PermissionCheck> hasAny(List<NamedPermission> permissions, String pluginId, String organizationName) {
        return has(permissions, pluginId, organizationName, (namedPerms, perm) -> namedPerms.stream().allMatch(p -> perm.has(p.getPermission())));
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(me.minidigger.hangar.model.Permission).None, T(me.minidigger.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<Permissions> showPermissions(String pluginId, String organizationName) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(pluginId, organizationName);
        return ResponseEntity.ok(new Permissions().type(scopedPerms.getLeft()).permissions(scopedPerms.getRight().toNamed()));
    }

    private Pair<PermissionType, Permission> getPermissionsInScope(String pluginId, String organizationName) {
        if (pluginId != null) {
            Permission perms = permissionService.getProjectPermissions(apiAuthInfo.getUser(), pluginId);
            return new ImmutablePair<>(PermissionType.PROJECT, perms);
        } else if (organizationName != null) {
            Permission perms = permissionService.getOrganizationPermissions(apiAuthInfo.getUser(), organizationName);
            return new ImmutablePair<>(PermissionType.ORGANIZATION, perms);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity<PermissionCheck> has(List<NamedPermission> perms, String pluginId, String organizationName, BiPredicate<List<NamedPermission>, Permission> check) {
        Pair<PermissionType, Permission> scopedPerms = getPermissionsInScope(pluginId, organizationName);
        return ResponseEntity.ok(new PermissionCheck().type(scopedPerms.getLeft()).result(check.test(perms, scopedPerms.getRight())));
    }
}

