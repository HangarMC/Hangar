package io.papermc.hangar.security.annotations.currentuser;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.authorization.HangarAuthorizationManager;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authorization manager for {@link CurrentUser} annotation.
 * Verifies that the user is accessing their own user resource.
 */
@Component
public class CurrentUserAuthorizationManager extends HangarAuthorizationManager {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation) {
        // Check if method or class has @CurrentUser annotation
        Method method = methodInvocation.getMethod();
        Class<?> targetClass = methodInvocation.getThis() != null ? methodInvocation.getThis().getClass() : method.getDeclaringClass();
        
        CurrentUser methodAnnotation = AnnotationUtils.findAnnotation(method, CurrentUser.class);
        CurrentUser classAnnotation = AnnotationUtils.findAnnotation(targetClass, CurrentUser.class);
        CurrentUser annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        
        if (annotation == null) {
            // Abstain if annotation not present
            return null;
        }
        
        Authentication auth = authentication.get();
        if (!(auth instanceof final HangarAuthenticationToken hangarAuthenticationToken)) {
            throw HangarApiException.forbidden();
        }
        
        // Check for global permission to edit all user settings
        if (hangarAuthenticationToken.getPrincipal().isAllowedGlobal(Permission.EditAllUserSettings)) {
            return this.granted();
        }
        
        // Evaluate SpEL expression to get the username
        final MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
            method.getDeclaringClass(),
            method,
            methodInvocation.getArguments(),
            this.parameterNameDiscoverer
        );
        
        final Object user = this.expressionParser.parseExpression(annotation.value()).getValue(context);
        final String userName;
        if (user instanceof UserTable) {
            userName = ((UserTable) user).getName();
        } else if (user instanceof String) {
            userName = (String) user;
        } else {
            throw new IllegalArgumentException(user + " is not supported for the CurrentUser check");
        }
        
        if (!hangarAuthenticationToken.getName().equals(userName)) {
            throw HangarApiException.forbidden();
        }
        
        return this.granted();
    }
}
