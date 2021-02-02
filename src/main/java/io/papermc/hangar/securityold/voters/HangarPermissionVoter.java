package io.papermc.hangar.securityold.voters;

import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.security.internal.HangarAuthenticationToken;
import io.papermc.hangar.securityold.attributes.PermissionAttribute;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class HangarPermissionVoter implements AccessDecisionVoter<MethodInvocation> {

    private final BiFunction<HangarAuthenticationToken, MethodInvocation, Collection<NamedPermission>> getUserPermissions;
    private final Predicate<HangarAuthenticationToken> authChecks;
    private final Predicate<PermissionAttribute> typeCheck;

    public HangarPermissionVoter(BiFunction<HangarAuthenticationToken, MethodInvocation, Collection<NamedPermission>> getUserPermissions, Predicate<HangarAuthenticationToken> authChecks, Predicate<PermissionAttribute> typeCheck) {
        this.getUserPermissions = getUserPermissions;
        this.authChecks = authChecks;
        this.typeCheck = typeCheck;
    }

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
        Collection<NamedPermission> requiredPermissions = collection.stream().filter(this::supports).map(PermissionAttribute.class::cast).filter(typeCheck).map(PermissionAttribute::getPermission).collect(Collectors.toSet());
        if (requiredPermissions.isEmpty() && (!(authentication instanceof HangarAuthenticationToken) || authentication.getPrincipal().equals("anonymousUser"))) {
            return ACCESS_ABSTAIN;
        }
        else if (!requiredPermissions.isEmpty() && (!(authentication instanceof HangarAuthenticationToken) || authentication.getPrincipal().equals("anonymousUser"))) {
            return ACCESS_DENIED;
        }

        HangarAuthenticationToken hangarAuth = (HangarAuthenticationToken) authentication;
        if (!authChecks.test(hangarAuth)) return ACCESS_DENIED;
        Collection<NamedPermission> userPermissions = getUserPermissions.apply(hangarAuth, object);
        if (!userPermissions.containsAll(requiredPermissions)) {
            System.out.println("Required perms: " + requiredPermissions.toString());
            System.out.println("User perms: " + userPermissions.toString());
            return ACCESS_DENIED;
        }
        return ACCESS_GRANTED;
    }
}
