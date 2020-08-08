package me.minidigger.hangar.config;

import me.minidigger.hangar.security.metadatasources.GlobalPermissionSource;
import me.minidigger.hangar.security.metadatasources.HangarMetadataSources;
import me.minidigger.hangar.security.metadatasources.ProjectPermissionSource;
import me.minidigger.hangar.security.voters.GlobalPermissionVoter;
import me.minidigger.hangar.service.PermissionService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

@Configuration
@AutoConfigureBefore(SecurityConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final PermissionService permissionService;

    @Autowired
    public MethodSecurityConfig(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return new HangarMetadataSources(new GlobalPermissionSource(), new ProjectPermissionSource());
    }

    @Override
    protected AccessDecisionManager accessDecisionManager() {
        AbstractAccessDecisionManager manager = (AbstractAccessDecisionManager) super.accessDecisionManager();
        manager.getDecisionVoters().add(new GlobalPermissionVoter(permissionService));
        return manager;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler() {
            @Override
            public StandardEvaluationContext createEvaluationContextInternal(Authentication auth, MethodInvocation mi) {
                StandardEvaluationContext evaluationContext = super.createEvaluationContextInternal(auth, mi);

                ((StandardTypeLocator) evaluationContext.getTypeLocator()).registerImport("me.minidigger.hangar.model");
                ((StandardTypeLocator) evaluationContext.getTypeLocator()).registerImport("me.minidigger.hangar.controller.util");

                return evaluationContext;
            }
        };
    }
}
