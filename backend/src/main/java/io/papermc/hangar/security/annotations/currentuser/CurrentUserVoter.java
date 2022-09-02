package io.papermc.hangar.security.annotations.currentuser;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.currentuser.CurrentUserMetadataExtractor.CurrentUserAttribute;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserVoter extends HangarDecisionVoter<CurrentUserAttribute> {

    public CurrentUserVoter() {
        super(CurrentUserAttribute.class);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation methodInvocation, @NotNull CurrentUserAttribute attribute) {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return ACCESS_DENIED;
        }
        HangarAuthenticationToken hangarAuthenticationToken = (HangarAuthenticationToken) authentication;
        if (hangarAuthenticationToken.getPrincipal().isAllowedGlobal(Permission.EditAllUserSettings)) {
            return ACCESS_GRANTED;
        }
        String userName;
        Object user = attribute.getExpression().getValue(getMethodEvaluationContext(methodInvocation));
        if (user instanceof UserTable) {
            userName = ((UserTable) user).getName();
        } else if (user instanceof String) {
            userName = (String) user;
        } else {
            throw new IllegalArgumentException(user + " is not supported for the CurrentUser check");
        }
        if (!hangarAuthenticationToken.getName().equals(userName)) {
            return ACCESS_DENIED;
        }
        return ACCESS_GRANTED;
    }

    @Override
    public void onAccessDenied() {
        throw HangarApiException.forbidden();
    }
}
