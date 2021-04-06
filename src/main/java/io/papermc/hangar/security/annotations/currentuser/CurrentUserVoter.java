package io.papermc.hangar.security.annotations.currentuser;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.HangarDecisionVoter;
import io.papermc.hangar.security.annotations.currentuser.CurrentUserMetadataExtractor.CurrentUserAttribute;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CurrentUserVoter extends HangarDecisionVoter<CurrentUserAttribute> {

    public CurrentUserVoter() {
        super(CurrentUserAttribute.class);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation methodInvocation, Collection<ConfigAttribute> attributes) {
        CurrentUserAttribute attribute = findAttribute(attributes);
        if (attribute == null) {
            return ACCESS_ABSTAIN;
        }
        if (!(authentication instanceof HangarAuthenticationToken)) {
            throw new HangarApiException(HttpStatus.FORBIDDEN);
        }
        HangarAuthenticationToken hangarAuthenticationToken = (HangarAuthenticationToken) authentication;
        if (hangarAuthenticationToken.getPrincipal().getGlobalPermissions().has(Permission.EditAllUserSettings)) {
            return ACCESS_GRANTED;
        }
        String userName;
        Object user = attribute.getExpression().getValue(new MethodBasedEvaluationContext(
                methodInvocation.getMethod().getDeclaringClass(),
                methodInvocation.getMethod(),
                methodInvocation.getArguments(),
                parameterNameDiscoverer
        ));
        if (user instanceof UserTable) {
            userName = ((UserTable) user).getName();
        } else if (user instanceof String) {
            userName = (String) user;
        } else {
            throw new IllegalArgumentException(user + " is not supported for the CurrentUser check");
        }
        if (!hangarAuthenticationToken.getName().equals(userName)) {
            throw new HangarApiException(HttpStatus.FORBIDDEN);
        }
        return ACCESS_GRANTED;
    }
}
