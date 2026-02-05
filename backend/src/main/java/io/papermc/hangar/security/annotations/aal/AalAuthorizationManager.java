package io.papermc.hangar.security.annotations.aal;

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
 * Authorization manager for {@link RequireAal} annotation.
 * Requires the user to have a minimum Account Authentication Level (AAL).
 */
@Component
public class AalAuthorizationManager extends HangarAuthorizationManager {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation methodInvocation) {
        // Check if method or class has @RequireAal annotation
        Method method = methodInvocation.getMethod();
        Class<?> targetClass = methodInvocation.getThis() != null ? methodInvocation.getThis().getClass() : method.getDeclaringClass();
        
        RequireAal methodAnnotation = AnnotationUtils.findAnnotation(method, RequireAal.class);
        RequireAal classAnnotation = AnnotationUtils.findAnnotation(targetClass, RequireAal.class);
        RequireAal annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        
        if (annotation == null) {
            // Abstain if annotation not present
            return null;
        }
        
        Authentication auth = authentication.get();
        if (!(auth instanceof HangarAuthenticationToken)) {
            return this.denied();
        }
        
        final int aal = ((HangarAuthenticationToken) auth).getPrincipal().getAal();
        if (aal < annotation.value()) {
            if (annotation.value() == 1) {
                throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.aal1");
            } else {
                throw new HangarApiException(HttpStatus.UNAUTHORIZED, "error.aal2");
            }
        }
        
        return this.granted();
    }
}
