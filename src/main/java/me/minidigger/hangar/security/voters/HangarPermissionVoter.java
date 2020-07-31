package me.minidigger.hangar.security.voters;

import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.security.HangarAuthentication;
import me.minidigger.hangar.security.PermissionAttribute;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class HangarPermissionVoter implements AccessDecisionVoter {

    private final Function<HangarAuthentication, Collection<NamedPermission>> getUserPermissions;
    private final Predicate<HangarAuthentication> authChecks;

    public HangarPermissionVoter(Function<HangarAuthentication, Collection<NamedPermission>> getUserPermissions, Predicate<HangarAuthentication> authChecks) {
        this.getUserPermissions = getUserPermissions;
        this.authChecks = authChecks;
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
        Collection<NamedPermission> requiredPermissions = ((Collection<ConfigAttribute>) collection).stream().filter(this::supports).map(PermissionAttribute.class::cast).map(PermissionAttribute::getPermission).collect(Collectors.toSet());
        if (requiredPermissions.isEmpty() && (!(authentication instanceof HangarAuthentication) || authentication.getPrincipal().equals("anonymousUser"))) {
            return ACCESS_ABSTAIN;
        }
        else if (!requiredPermissions.isEmpty() && (!(authentication instanceof HangarAuthentication) || authentication.getPrincipal().equals("anonymousUser"))) {
            return ACCESS_DENIED;
        }

        HangarAuthentication hangarAuth = (HangarAuthentication) authentication;
        if (!authChecks.test(hangarAuth)) return ACCESS_DENIED;
        Collection<NamedPermission> userPermissions = getUserPermissions.apply(hangarAuth);
        if (!userPermissions.containsAll(requiredPermissions)) {
            System.out.println("Required perms: " + requiredPermissions.toString());
            System.out.println("User perms: " + userPermissions.toString());
            return ACCESS_DENIED;
        }
        return ACCESS_GRANTED;
    }
}
