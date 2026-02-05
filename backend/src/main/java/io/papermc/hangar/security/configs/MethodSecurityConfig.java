package io.papermc.hangar.security.configs;

import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.currentuser.CurrentUser;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.privileged.Privileged;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.authorization.HangarAuthorizationManager;
import io.papermc.hangar.security.authorization.HangarUnanimousAuthorizationManager;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@AutoConfigureBefore(SecurityConfig.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig {

    /**
     * Creates a method interceptor for our custom authorization logic.
     * This interceptor is applied only to methods with our custom annotations
     * and does not interfere with Spring's built-in annotations like @PreAuthorize.
     * All HangarAuthorizationManager beans are automatically autowired and included.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor customAuthorizationMethodInterceptor(List<HangarAuthorizationManager> authorizationManagers) {
        
        // Create a unanimous-based authorization manager with all custom managers
        AuthorizationManager<MethodInvocation> authorizationManager = 
            new HangarUnanimousAuthorizationManager(authorizationManagers);
        
        // Create a pointcut that matches our custom annotations
        Pointcut pointcut = this.customAnnotationsPointcut();
        
        // Create and return the method interceptor with the custom pointcut
        return AuthorizationManagerBeforeMethodInterceptor.authorization(pointcut, authorizationManager);
    }
    
    /**
     * Creates a pointcut that matches methods or classes annotated with our custom security annotations.
     * This ensures the interceptor only applies to our custom annotations and doesn't interfere
     * with Spring's built-in security annotations like @PreAuthorize, @Secured, etc.
     */
    private Pointcut customAnnotationsPointcut() {
        // Start with PermissionRequired
        ComposablePointcut pointcut = new ComposablePointcut(
            new AnnotationMatchingPointcut(PermissionRequired.class, true)
        ).union(new AnnotationMatchingPointcut(null, PermissionRequired.class, true));
        
        // Add Privileged
        pointcut = pointcut.union(new AnnotationMatchingPointcut(Privileged.class, true));
        pointcut = pointcut.union(new AnnotationMatchingPointcut(null, Privileged.class, true));
        
        // Add Unlocked
        pointcut = pointcut.union(new AnnotationMatchingPointcut(Unlocked.class, true));
        pointcut = pointcut.union(new AnnotationMatchingPointcut(null, Unlocked.class, true));
        
        // Add RequireAal
        pointcut = pointcut.union(new AnnotationMatchingPointcut(RequireAal.class, true));
        pointcut = pointcut.union(new AnnotationMatchingPointcut(null, RequireAal.class, true));
        
        // Add CurrentUser
        pointcut = pointcut.union(new AnnotationMatchingPointcut(CurrentUser.class, true));
        pointcut = pointcut.union(new AnnotationMatchingPointcut(null, CurrentUser.class, true));
        
        // Add VisibilityRequired
        pointcut = pointcut.union(new AnnotationMatchingPointcut(VisibilityRequired.class, true));
        pointcut = pointcut.union(new AnnotationMatchingPointcut(null, VisibilityRequired.class, true));
        
        return pointcut;
    }
}
