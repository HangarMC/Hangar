package io.papermc.hangar.security.authorization;

import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

/**
 * Base class for Hangar's custom authorization managers.
 * Provides common utilities for method invocation authorization.
 */
public abstract class HangarAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    /**
     * Check authorization for the given method invocation.
     * 
     * @param authentication the authentication object
     * @param methodInvocation the method invocation being secured
     * @return an AuthorizationDecision indicating whether access is granted
     */
    @Override
    public abstract AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation);

    /**
     * Helper method to create a granted decision.
     */
    protected AuthorizationDecision granted() {
        return new AuthorizationDecision(true);
    }

    /**
     * Helper method to create a denied decision.
     */
    protected AuthorizationDecision denied() {
        return new AuthorizationDecision(false);
    }
}
