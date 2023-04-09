package io.papermc.hangar.security.annotations.aal;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AalUnlockedVoter extends HangarDecisionVoter<AalMetadataExtractor.AalAttribute> {

    public AalUnlockedVoter() {
        super(AalMetadataExtractor.AalAttribute.class);
    }

    @Override
    public int vote(final Authentication authentication, final MethodInvocation object, final @NotNull AalMetadataExtractor.AalAttribute attribute) {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return ACCESS_DENIED;
        }
        final int aal = ((HangarAuthenticationToken) authentication).getPrincipal().getAal();
        if (aal < attribute.aal()) {
            if (attribute.aal() == 1) {
                throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.aal1");
            } else {
                throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.aal2");
            }
        }
        return ACCESS_GRANTED;
    }
}
