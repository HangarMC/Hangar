package io.papermc.hangar.security.annotations.privileged;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PrivilegedVoter extends HangarDecisionVoter<PrivilegedMetadataExtractor.PrivilegedUnlockedAttribute> {

    public PrivilegedVoter() {
        super(PrivilegedMetadataExtractor.PrivilegedUnlockedAttribute.class);
    }

    @Override
    public int vote(final Authentication authentication, final MethodInvocation object, final @NotNull PrivilegedMetadataExtractor.PrivilegedUnlockedAttribute attribute) {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return ACCESS_DENIED;
        }
        if (!((HangarAuthenticationToken) authentication).getPrincipal().isPrivileged()) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.privileged");
        }
        return ACCESS_GRANTED;
    }
}
