package io.papermc.hangar.securityold.voters;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.securityold.attributes.PermissionAttribute;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class HangarPermissionVoter implements AccessDecisionVoter<MethodInvocation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HangarPermissionVoter.class);

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attribute instanceof PermissionAttribute;
    }

    @Override
    public boolean supports(Class clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation object, Collection<ConfigAttribute> collection) {
        Collection<NamedPermission> requiredPermissions = collection.stream()
                .filter(this::supports)
                .map(PermissionAttribute.class::cast)
                .filter(this::checkPermissionType)
                .map(PermissionAttribute::getPermission)
                .collect(Collectors.toSet());

        if (!(authentication instanceof HangarAuthenticationToken) || authentication.getPrincipal().equals("anonymousUser")) {
            return requiredPermissions.isEmpty() ? ACCESS_ABSTAIN : ACCESS_DENIED;
        }

        HangarAuthenticationToken hangarAuth = (HangarAuthenticationToken) authentication;
        if (!checkAuthentication(hangarAuth)) return ACCESS_DENIED;
        Collection<NamedPermission> userPermissions = getUserPermissions(hangarAuth, object);
        if (!userPermissions.containsAll(requiredPermissions)) {
            LOGGER.info("Required perms: {}", requiredPermissions);
            LOGGER.info("User perms: {}", userPermissions);
            return ACCESS_DENIED;
        }
        return ACCESS_GRANTED;
    }

    protected abstract Collection<NamedPermission> getUserPermissions(
            HangarAuthenticationToken hangarAuthenticationToken,
            MethodInvocation methodInvocation
    );

    protected abstract boolean checkAuthentication(HangarAuthenticationToken hangarAuthenticationToken);

    protected abstract boolean checkPermissionType(PermissionAttribute permissionAttribute);
}
