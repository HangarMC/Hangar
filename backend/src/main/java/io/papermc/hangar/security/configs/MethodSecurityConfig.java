package io.papermc.hangar.security.configs;

import io.papermc.hangar.security.authorization.AalAuthorizationManager;
import io.papermc.hangar.security.authorization.CurrentUserAuthorizationManager;
import io.papermc.hangar.security.authorization.HangarUnanimousAuthorizationManager;
import io.papermc.hangar.security.authorization.PermissionRequiredAuthorizationManager;
import io.papermc.hangar.security.authorization.PrivilegedAuthorizationManager;
import io.papermc.hangar.security.authorization.UnlockedAuthorizationManager;
import io.papermc.hangar.security.authorization.VisibilityRequiredAuthorizationManager;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
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
     * This interceptor is applied to all methods and checks custom annotations.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor customAuthorizationMethodInterceptor(
            PermissionRequiredAuthorizationManager permissionRequiredAuthorizationManager,
            PrivilegedAuthorizationManager privilegedAuthorizationManager,
            UnlockedAuthorizationManager unlockedAuthorizationManager,
            AalAuthorizationManager aalAuthorizationManager,
            CurrentUserAuthorizationManager currentUserAuthorizationManager,
            VisibilityRequiredAuthorizationManager visibilityRequiredAuthorizationManager) {
        
        // Create a unanimous-based authorization manager with all custom managers
        AuthorizationManager<MethodInvocation> authorizationManager = new HangarUnanimousAuthorizationManager(List.of(
            permissionRequiredAuthorizationManager,
            privilegedAuthorizationManager,
            unlockedAuthorizationManager,
            aalAuthorizationManager,
            currentUserAuthorizationManager,
            visibilityRequiredAuthorizationManager
        ));
        
        // Create and return the method interceptor
        return AuthorizationManagerBeforeMethodInterceptor.preAuthorize(authorizationManager);
    }
}
