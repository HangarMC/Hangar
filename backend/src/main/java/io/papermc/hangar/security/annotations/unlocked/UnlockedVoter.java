package io.papermc.hangar.security.annotations.unlocked;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UnlockedVoter extends HangarDecisionVoter<UnlockedMetadataExtractor.UnlockedAttribute> {

    public UnlockedVoter() {
        super(UnlockedMetadataExtractor.UnlockedAttribute.class);
    }

    @Override
    public int vote(final Authentication authentication, final MethodInvocation object, final @NotNull UnlockedMetadataExtractor.UnlockedAttribute attribute) {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return ACCESS_DENIED;
        }
        if (((HangarAuthenticationToken) authentication).getPrincipal().isLocked()) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.userLocked");
        }
        return ACCESS_GRANTED;
    }
}
