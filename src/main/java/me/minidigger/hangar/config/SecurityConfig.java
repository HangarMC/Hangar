package me.minidigger.hangar.config;

import me.minidigger.hangar.filter.HangarAuthenticationFilter;
import me.minidigger.hangar.security.HangarAuthenticationProvider;
import me.minidigger.hangar.security.voters.GlobalPermissionVoter;
import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.RouteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final HangarAuthenticationProvider authProvider;
    private final RouteHelper routeHelper;
    private final PermissionService permissionService;
    private final ProjectService projectService;

    @Autowired
    public SecurityConfig(HangarAuthenticationProvider authProvider, RouteHelper routeHelper, PermissionService permissionService, ProjectService projectService) {
        this.authProvider = authProvider;
        this.routeHelper = routeHelper;
        this.permissionService = permissionService;
        this.projectService = projectService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers(
                "/api/v2/authenticate", "/api/v2/sessions/current", "/api/v2/keys"
        );

        http.addFilter(new HangarAuthenticationFilter());

        http.exceptionHandling().authenticationEntryPoint((request, response, e) -> response.sendRedirect(routeHelper.getRouteUrl("users.login", "", "", request.getRequestURI())));

        http.authorizeRequests().anyRequest().permitAll().accessDecisionManager(accessDecisionManager()); // we use method security
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoters = Arrays.asList(
                new WebExpressionVoter(),
                new RoleVoter(),
                new AuthenticatedVoter(),
                new GlobalPermissionVoter(permissionService)
        );
        return new UnanimousBased(decisionVoters);
    }
}
