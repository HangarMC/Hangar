package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.permission.PermissionRequiredMetadataExtractor.PermissionRequiredAttribute;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.service.PermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class PermissionRequiredVoter extends HangarDecisionVoter<PermissionRequiredAttribute> {

    private final PermissionService permissionService;

    @Autowired
    public PermissionRequiredVoter(PermissionService permissionService) {
        super(PermissionRequiredAttribute.class);
        this.permissionService = permissionService;
        this.setAllowMultipleAttributes(true);
    }

    // TODO debug logging
    @Override
    public int vote(Authentication authentication, MethodInvocation methodInvocation, Set<PermissionRequiredAttribute> attributes) {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        HangarAuthenticationToken hangarAuthenticationToken = (HangarAuthenticationToken) authentication;
        for (PermissionRequiredAttribute attribute : attributes) {
            Object[] arguments = attribute.getExpression().getValue(getMethodEvaluationContext(methodInvocation), Object[].class);
            if (arguments == null || !attribute.getPermissionType().getArgCounts().contains(arguments.length)) {
                throw new IllegalStateException("Bad annotation configuration");
            }
            Permission requiredPerm = Arrays.stream(attribute.getPermissions()).map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
            Permission currentPerm;
            switch (attribute.getPermissionType()) {
                case PROJECT:
                    if (arguments.length == 1) {
                        currentPerm = permissionService.getProjectPermissions(hangarAuthenticationToken.getUserId(), (long) arguments[0]);
                    } else if (arguments.length == 2) {
                        currentPerm = permissionService.getProjectPermissions(hangarAuthenticationToken.getUserId(), (String) arguments[0], (String) arguments[1]);
                    } else {
                        currentPerm = Permission.None;
                    }
                    break;
                case ORGANIZATION:
                    if (arguments.length == 1) {
                        currentPerm = permissionService.getOrganizationPermissions(hangarAuthenticationToken.getUserId(), (String) arguments[0]);
                    } else {
                        currentPerm = Permission.None;
                    }
                    break;
                case GLOBAL:
                    currentPerm = permissionService.getGlobalPermissions(hangarAuthenticationToken.getUserId());
                    break;
                default:
                    currentPerm = Permission.None;
            }
            if (hangarAuthenticationToken.getPrincipal().isAllowed(requiredPerm, currentPerm)) {
                return ACCESS_GRANTED;
            }
        }
        throw new HangarApiException(HttpStatus.NOT_FOUND);
    }
}
