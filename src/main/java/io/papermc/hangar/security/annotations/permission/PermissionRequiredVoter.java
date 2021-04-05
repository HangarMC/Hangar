package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.permission.PermissionRequiredMetadataExtractor.PermissionRequiredAttribute;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.service.PermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class PermissionRequiredVoter extends HangarDecisionVoter<PermissionRequiredAttribute> {

    private final PermissionService permissionService;

    @Autowired
    public PermissionRequiredVoter(PermissionService permissionService) {
        super(PermissionRequiredAttribute.class);
        this.permissionService = permissionService;
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation methodInvocation, Collection<ConfigAttribute> attributes) {
        Collection<PermissionRequiredAttribute> permAttributes = findAttributes(attributes);
        if (permAttributes.isEmpty()) {
            return ACCESS_ABSTAIN;
        }
        if (!(authentication instanceof HangarAuthenticationToken)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        HangarAuthenticationToken hangarAuthenticationToken = (HangarAuthenticationToken) authentication;
        int result = ACCESS_DENIED;
        for (PermissionRequiredAttribute attribute : permAttributes) {
            Object[] arguments = attribute.getExpression().getValue(new MethodBasedEvaluationContext(
                    methodInvocation.getMethod().getDeclaringClass(),
                    methodInvocation.getMethod(),
                    methodInvocation.getArguments(),
                    parameterNameDiscoverer
            ), Object[].class);
            if (arguments == null || !attribute.getPermissionType().getArgCounts().contains(arguments.length)) {
                throw new IllegalStateException("Bad annotation configuration");
            }
            switch (attribute.getPermissionType()) {
                case PROJECT:
                    if (arguments.length == 1 && permissionService.getProjectPermissions(hangarAuthenticationToken.getUserId(), (long) arguments[0]).hasAll(attribute.getPermissions())) {
                        result = ACCESS_GRANTED;
                    } else if (arguments.length == 2 && permissionService.getProjectPermissions(hangarAuthenticationToken.getUserId(), (String) arguments[0], (String) arguments[1]).hasAll(attribute.getPermissions())) {
                        result = ACCESS_GRANTED;
                    } else {
                        result = ACCESS_DENIED;
                    }
                    break;
                case ORGANIZATION:
                    if (arguments.length == 1 && permissionService.getOrganizationPermissions(hangarAuthenticationToken.getUserId(), (String) arguments[0]).hasAll(attribute.getPermissions())) {
                        result = ACCESS_GRANTED;
                    } else {
                        result = ACCESS_DENIED;
                    }
                    break;
                case GLOBAL:
                    if (permissionService.getGlobalPermissions(hangarAuthenticationToken.getUserId()).hasAll(attribute.getPermissions())) {
                        result = ACCESS_GRANTED;
                    } else {
                        result = ACCESS_DENIED;
                    }
                    break;
            }
            if (result == ACCESS_GRANTED) {
                return ACCESS_GRANTED;
            }
        }
        throw new HangarApiException(HttpStatus.NOT_FOUND);
    }
}
