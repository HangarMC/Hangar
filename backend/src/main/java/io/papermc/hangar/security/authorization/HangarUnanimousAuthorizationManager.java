package io.papermc.hangar.security.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

/**
 * Composite authorization manager that requires unanimous approval from all managers.
 * This replaces the legacy HangarUnanimousBased AccessDecisionManager.
 * 
 * <p>If any manager denies access, the decision is denied.
 * If all managers grant or abstain (return null), the decision is granted if at least one granted.</p>
 */
public class HangarUnanimousAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    private final List<AuthorizationManager<MethodInvocation>> authorizationManagers;

    public HangarUnanimousAuthorizationManager(List<AuthorizationManager<MethodInvocation>> authorizationManagers) {
        this.authorizationManagers = new ArrayList<>(authorizationManagers);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation) {
        int grants = 0;
        
        for (AuthorizationManager<MethodInvocation> manager : this.authorizationManagers) {
            AuthorizationDecision decision = manager.check(authentication, methodInvocation);
            
            // null means abstain
            if (decision == null) {
                continue;
            }
            
            if (decision.isGranted()) {
                grants++;
            } else {
                // Any denial results in overall denial (unanimous voting)
                return new AuthorizationDecision(false);
            }
        }
        
        // If at least one manager granted, allow access
        // Otherwise, allow if all abstained (allowIfAllAbstainDecisions behavior)
        return new AuthorizationDecision(grants > 0 || this.authorizationManagers.isEmpty());
    }
}
