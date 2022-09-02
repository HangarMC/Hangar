package io.papermc.hangar.security.annotations.unlocked;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.unlocked.UnlockedMetadataExtractor.UnlockedAttribute;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UnlockedVoter extends HangarDecisionVoter<UnlockedAttribute> {

    public UnlockedVoter() {
        super(UnlockedAttribute.class);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation object, @NotNull UnlockedAttribute attribute) {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return ACCESS_DENIED;
        }
        if (((HangarAuthenticationToken) authentication).getPrincipal().isLocked()) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.userLocked");
        }
        return ACCESS_GRANTED;
    }
}
