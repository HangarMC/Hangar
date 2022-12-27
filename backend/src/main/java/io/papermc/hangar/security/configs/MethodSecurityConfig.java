package io.papermc.hangar.security.configs;

import io.papermc.hangar.security.HangarMetadataSources;
import io.papermc.hangar.security.HangarUnanimousBased;
import java.util.ArrayList;
import java.util.List;
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

@Configuration
@AutoConfigureBefore(SecurityConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final ApplicationContext applicationContext;
    private final List<AnnotationMetadataExtractor> annotationMetadataExtractors;
    private final List<AccessDecisionVoter<?>> accessDecisionVoters;

    @Autowired
    public MethodSecurityConfig(final ApplicationContext applicationContext, final List<AnnotationMetadataExtractor> annotationMetadataExtractors, final List<AccessDecisionVoter<?>> accessDecisionVoters) {
        this.applicationContext = applicationContext;
        this.annotationMetadataExtractors = annotationMetadataExtractors;
        this.accessDecisionVoters = accessDecisionVoters;
    }

    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return new HangarMetadataSources(this.annotationMetadataExtractors);
    }

    @Override
    protected AccessDecisionManager accessDecisionManager() {
        final List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        final ExpressionBasedPreInvocationAdvice expressionAdvice =
            new ExpressionBasedPreInvocationAdvice();
        expressionAdvice.setExpressionHandler(this.getExpressionHandler());
        decisionVoters.add(new PreInvocationAuthorizationAdviceVoter(expressionAdvice));
        decisionVoters.add(new Jsr250Voter());
        final RoleVoter roleVoter = new RoleVoter();
        try {
            final GrantedAuthorityDefaults grantedAuthorityDefaults = this.applicationContext.getBean(GrantedAuthorityDefaults.class);
            roleVoter.setRolePrefix(grantedAuthorityDefaults.getRolePrefix());
        } catch (final BeansException ignored) {
        }
        decisionVoters.add(roleVoter);
        decisionVoters.add(new AuthenticatedVoter());
        decisionVoters.addAll(this.accessDecisionVoters);
        final UnanimousBased accessDecisionManager = new HangarUnanimousBased(decisionVoters);
        accessDecisionManager.setAllowIfAllAbstainDecisions(true);
        return accessDecisionManager;
    }
}
