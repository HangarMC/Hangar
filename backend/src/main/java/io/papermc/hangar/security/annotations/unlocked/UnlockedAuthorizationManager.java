package io.papermc.hangar.security.annotations.unlocked;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.authorization.HangarAuthorizationManager;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authorization manager for {@link Unlocked} annotation.
 * Requires the user account to not be locked.
 */
@Component
public class UnlockedAuthorizationManager extends HangarAuthorizationManager {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation) {
        // Check if method or class has @Unlocked annotation
        Method method = methodInvocation.getMethod();
        Class<?> targetClass = methodInvocation.getThis() != null ? methodInvocation.getThis().getClass() : method.getDeclaringClass();
        
        boolean hasAnnotation = AnnotationUtils.findAnnotation(method, Unlocked.class) != null ||
                               AnnotationUtils.findAnnotation(targetClass, Unlocked.class) != null;
        
        if (!hasAnnotation) {
            // Abstain if annotation not present
            return null;
        }
        
        Authentication auth = authentication.get();
        if (!(auth instanceof HangarAuthenticationToken)) {
            return this.denied();
        }
        
        if (((HangarAuthenticationToken) auth).getPrincipal().isLocked()) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.userLocked");
        }
        
        return this.granted();
    }
}
