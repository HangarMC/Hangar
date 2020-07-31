package me.minidigger.hangar.config;

import me.minidigger.hangar.security.metadatasources.HangarMetadataSources;
import me.minidigger.hangar.security.metadatasources.GlobalPermissionSource;
import me.minidigger.hangar.security.metadatasources.ProjectPermissionSource;
import me.minidigger.hangar.security.voters.GlobalPermissionVoter;
import me.minidigger.hangar.security.voters.ProjectPermissionVoter;
import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.project.ProjectService;
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
    private final ProjectService projectService;

    @Autowired
    public MethodSecurityConfig(PermissionService permissionService, ProjectService projectService) {
        this.permissionService = permissionService;
        this.projectService = projectService;
    }

//    @Override
//    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
//        return new HangarMetadataSources(new GlobalPermissionSource(), new ProjectPermissionSource());
//    }

//    @Override
//    protected AccessDecisionManager accessDecisionManager() {
//        AbstractAccessDecisionManager manager = (AbstractAccessDecisionManager) super.accessDecisionManager();
//        manager.getDecisionVoters().add(new GlobalPermissionVoter(permissionService));
//        manager.getDecisionVoters().add(new ProjectPermissionVoter(projectService, permissionService));
//        return manager;
//    }
}
