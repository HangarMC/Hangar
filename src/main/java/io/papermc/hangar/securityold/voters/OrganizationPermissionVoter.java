package io.papermc.hangar.securityold.voters;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.securityold.attributes.PermissionAttribute;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.util.StringUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Set;

public class OrganizationPermissionVoter extends HangarPermissionVoter {
    private final PermissionService permissionService;

    public OrganizationPermissionVoter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected Collection<NamedPermission> getUserPermissions(HangarAuthenticationToken hangarAuthenticationToken, MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        if (method.getParameterCount() == 0) return Set.of();
        String organizationName = null;
        Object[] arguments = methodInvocation.getArguments();

        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            PathVariable pathVarAnnotation = parameter.getAnnotation(PathVariable.class);
            if (pathVarAnnotation == null) {
                continue;
            }

            if (StringUtils.isAnyEqualIgnoreCase("organization",
                    pathVarAnnotation.name(),
                    pathVarAnnotation.value(),
                    parameter.getName())) {
                organizationName = arguments[i].toString();
            }
        }

        if (organizationName == null) {
            return Set.of();
        }

        return permissionService.getOrganizationPermissions(hangarAuthenticationToken.getUserId(), organizationName).toNamed();
    }

    @Override
    protected boolean checkAuthentication(HangarAuthenticationToken hangarAuthenticationToken) {
        return true;
    }

    @Override
    protected boolean checkPermissionType(PermissionAttribute permissionAttribute) {
        return PermissionAttribute.ORG_TYPE.equals(permissionAttribute.getType());
    }
}
