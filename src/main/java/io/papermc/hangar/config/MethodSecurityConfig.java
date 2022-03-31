package io.papermc.hangar.config;

import io.papermc.hangar.security.metadatasources.GlobalPermissionSource;
import io.papermc.hangar.security.metadatasources.HangarMetadataSources;
import io.papermc.hangar.security.metadatasources.OrganizationPermissionSource;
import io.papermc.hangar.security.metadatasources.ProjectPermissionSource;
import io.papermc.hangar.security.metadatasources.UserLockSource;
import io.papermc.hangar.security.voters.GlobalPermissionVoter;
import io.papermc.hangar.security.voters.OrganizationPermissionVoter;
import io.papermc.hangar.security.voters.ProjectPermissionVoter;
import io.papermc.hangar.security.voters.UserLockVoter;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.UserService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
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
    private final PermissionService permissionService;
    private final UserService userService;

    @Autowired
    public MethodSecurityConfig(ApplicationContext applicationContext, PermissionService permissionService, UserService userService) {
        this.applicationContext = applicationContext;
        this.permissionService = permissionService;
        this.userService = userService;
    }

    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return new HangarMetadataSources(new GlobalPermissionSource(), new ProjectPermissionSource(), new UserLockSource(), new OrganizationPermissionSource());
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
        } catch (BeansException ignored) { }
        decisionVoters.add(roleVoter);
        decisionVoters.add(new AuthenticatedVoter());
        decisionVoters.add(new ProjectPermissionVoter(permissionService));
        decisionVoters.add(new OrganizationPermissionVoter(permissionService));
        decisionVoters.add(new GlobalPermissionVoter(permissionService));
        decisionVoters.add(new UserLockVoter(userService));
        return new UnanimousBased(decisionVoters);
    }
}
