package io.papermc.hangar.security.voters;

import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.security.HangarAuthentication;
import io.papermc.hangar.security.attributes.PermissionAttribute;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class HangarPermissionVoter implements AccessDecisionVoter {

    private final BiFunction<HangarAuthentication, MethodInvocation, Collection<NamedPermission>> getUserPermissions;
    private final Predicate<HangarAuthentication> authChecks;
    private final Predicate<PermissionAttribute> typeCheck;

    public HangarPermissionVoter(BiFunction<HangarAuthentication, MethodInvocation, Collection<NamedPermission>> getUserPermissions, Predicate<HangarAuthentication> authChecks, Predicate<PermissionAttribute> typeCheck) {
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
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection collection) {
        if (!(object instanceof MethodInvocation)) return ACCESS_ABSTAIN;
        MethodInvocation method = (MethodInvocation) object;
        Collection<NamedPermission> requiredPermissions = ((Collection<ConfigAttribute>) collection).stream().filter(this::supports).map(PermissionAttribute.class::cast).filter(typeCheck).map(PermissionAttribute::getPermission).collect(Collectors.toSet());
        if (requiredPermissions.isEmpty() && (!(authentication instanceof HangarAuthentication) || authentication.getPrincipal().equals("anonymousUser"))) {
            return ACCESS_ABSTAIN;
        }
        else if (!requiredPermissions.isEmpty() && (!(authentication instanceof HangarAuthentication) || authentication.getPrincipal().equals("anonymousUser"))) {
            return ACCESS_DENIED;
        }

        HangarAuthentication hangarAuth = (HangarAuthentication) authentication;
        if (!authChecks.test(hangarAuth)) return ACCESS_DENIED;
        Collection<NamedPermission> userPermissions = getUserPermissions.apply(hangarAuth, method);
        if (!userPermissions.containsAll(requiredPermissions)) {
            System.out.println("Required perms: " + requiredPermissions.toString());
            System.out.println("User perms: " + userPermissions.toString());
            return ACCESS_DENIED;
        }
        return ACCESS_GRANTED;
    }
}
