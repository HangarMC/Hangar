package io.papermc.hangar.config;

import io.papermc.hangar.security.metadatasources.GlobalPermissionSource;
import io.papermc.hangar.security.metadatasources.HangarMetadataSources;
import io.papermc.hangar.security.metadatasources.ProjectPermissionSource;
import io.papermc.hangar.security.voters.GlobalPermissionVoter;
import io.papermc.hangar.service.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

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
}
