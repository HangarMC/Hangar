package io.papermc.hangar.security.configs;

import io.papermc.hangar.securityold.metadatasources.HangarMetadataSources;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;
import org.springframework.security.access.annotation.Jsr250Voter;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.core.GrantedAuthorityDefaults;

import java.util.ArrayList;
import java.util.List;

@Configuration
@AutoConfigureBefore(SecurityConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final ApplicationContext applicationContext;
    private final List<AnnotationMetadataExtractor> annotationMetadataExtractors;
    private final List<AccessDecisionVoter<?>> accessDecisionVoters;

    @Autowired
    public MethodSecurityConfig(ApplicationContext applicationContext, List<AnnotationMetadataExtractor> annotationMetadataExtractors, List<AccessDecisionVoter<?>> accessDecisionVoters) {
        this.applicationContext = applicationContext;
        this.annotationMetadataExtractors = annotationMetadataExtractors;
        this.accessDecisionVoters = accessDecisionVoters;
    }

    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return new HangarMetadataSources(annotationMetadataExtractors);
    }

    @Override
    protected AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        ExpressionBasedPreInvocationAdvice expressionAdvice =
                new ExpressionBasedPreInvocationAdvice();
        expressionAdvice.setExpressionHandler(getExpressionHandler());
        decisionVoters.add(new PreInvocationAuthorizationAdviceVoter(expressionAdvice));
        decisionVoters.add(new Jsr250Voter());
        RoleVoter roleVoter = new RoleVoter();
        try {
            GrantedAuthorityDefaults grantedAuthorityDefaults = applicationContext.getBean(GrantedAuthorityDefaults.class);
            roleVoter.setRolePrefix(grantedAuthorityDefaults.getRolePrefix());
        } catch (BeansException ignored) {
        }
        decisionVoters.add(roleVoter);
        decisionVoters.add(new AuthenticatedVoter());
        decisionVoters.addAll(accessDecisionVoters);
        UnanimousBased accessDecisionManager = new UnanimousBased(decisionVoters);
        accessDecisionManager.setAllowIfAllAbstainDecisions(true);
        return accessDecisionManager;
    }
}
