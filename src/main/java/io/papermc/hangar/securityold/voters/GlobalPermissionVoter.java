package io.papermc.hangar.securityold.voters;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.securityold.attributes.PermissionAttribute;
import io.papermc.hangar.service.PermissionService;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Collection;

public class GlobalPermissionVoter extends HangarPermissionVoter {

    private final PermissionService permissionService;

    public GlobalPermissionVoter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected Collection<NamedPermission> getUserPermissions(HangarAuthenticationToken hangarAuthenticationToken, MethodInvocation methodInvocation) {
        return permissionService.getGlobalPermissions(hangarAuthenticationToken.getUserId()).toNamed();
    }

    @Override
    protected boolean checkAuthentication(HangarAuthenticationToken hangarAuthenticationToken) {
        return true;
    }

    @Override
    protected boolean checkPermissionType(PermissionAttribute permissionAttribute) {
        return PermissionAttribute.GLOBAL_TYPE.equals(permissionAttribute.getType());
    }
}
