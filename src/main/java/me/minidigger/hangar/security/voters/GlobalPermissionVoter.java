package me.minidigger.hangar.security.voters;

import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.security.attributes.PermissionAttribute;
import me.minidigger.hangar.service.PermissionService;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalPermissionVoter implements AccessDecisionVoter {

    private final PermissionService permissionService;

    public GlobalPermissionVoter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attribute instanceof PermissionAttribute;
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection collection) {
        Permission globalPerm = permissionService.getGlobalPermission(authentication.getName());
        if (globalPerm == null) globalPerm = !authentication.getPrincipal().equals("anonymousUser") ? Permission.HardDeleteProject.add(Permission.SeeHidden) : Permission.None; // TODO testing

        Set<NamedPermission> requiredPermissions =  ((Collection<ConfigAttribute>) collection).stream().filter(this::supports).map(PermissionAttribute.class::cast).map(PermissionAttribute::getPermission).collect(Collectors.toSet());
        Collection<NamedPermission> userGlobalPermissions = globalPerm.toNamed();
        if (!userGlobalPermissions.containsAll(requiredPermissions)) {
            System.out.println("Required perms: " + requiredPermissions.toString());
            System.out.println("User perms: " + userGlobalPermissions.toString());
            return ACCESS_DENIED;
        }
        return ACCESS_GRANTED;
    }
}
