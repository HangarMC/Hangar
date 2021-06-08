package io.papermc.hangar.security;

import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.List;

/**
 * So the default {@link UnanimousBased} decision manager
 * only passed one config attribute at a time which we don't want.
 * We want the voters to be unanimous, not the attributes themselves.
 */
public class HangarUnanimousBased extends UnanimousBased {

    public HangarUnanimousBased(List<AccessDecisionVoter<?>> decisionVoters) {
        super(decisionVoters);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) throws AccessDeniedException {
        int grant = 0;
        for (AccessDecisionVoter voter : getDecisionVoters()) {
            int result = voter.vote(authentication, object, attributes);
            switch (result) {
                case AccessDecisionVoter.ACCESS_GRANTED:
                    grant++;
                    break;
                case AccessDecisionVoter.ACCESS_DENIED:
                    if (voter instanceof HangarDecisionVoter) {
                        ((HangarDecisionVoter) voter).onAccessDenied();
                    }
                    throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
                default:
                    break;
            }
        }
        if (grant > 0) {
            return;
        }

        checkAllowIfAllAbstainDecisions();
    }
}
