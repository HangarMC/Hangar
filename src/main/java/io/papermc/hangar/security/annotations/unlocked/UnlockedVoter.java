package io.papermc.hangar.security.annotations.unlocked;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.security.annotations.unlocked.UnlockedMetadataExtractor.UnlockedAttribute;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UnlockedVoter implements AccessDecisionVoter<MethodInvocation> {

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attribute instanceof UnlockedAttribute;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation object, Collection<ConfigAttribute> attributes) {
        UnlockedAttribute attribute = findUnlockedAttribute(attributes);
        if (attribute == null) {
            return ACCESS_ABSTAIN;
        }
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return ACCESS_DENIED;
        }
        if (((HangarAuthenticationToken) authentication).getPrincipal().isLocked()) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.user.locked");
        }
        return ACCESS_GRANTED;
    }

    private UnlockedAttribute findUnlockedAttribute(Collection<ConfigAttribute> attributes) {
        for (ConfigAttribute attribute : attributes) {
            if (attribute instanceof UnlockedAttribute) {
                return (UnlockedAttribute) attribute;
            }
        }
        return null;
    }
}
