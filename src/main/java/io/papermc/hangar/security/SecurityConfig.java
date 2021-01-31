package io.papermc.hangar.security;

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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Arrays;
import java.util.List;

import io.papermc.hangar.security.entrypoints.HangarAuthenticationEntryPoint;
import io.papermc.hangar.security.voters.GlobalPermissionVoter;
import io.papermc.hangar.security.voters.OrganizationPermissionVoter;
import io.papermc.hangar.security.voters.ProjectPermissionVoter;
import io.papermc.hangar.security.voters.UserLockVoter;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.serviceold.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final HangarAuthenticationProvider authProvider;
    private final PermissionService permissionService;
    private final UserService userService;

    @Autowired
    public SecurityConfig(HangarAuthenticationProvider authProvider, PermissionService permissionService, UserService userService) {
        this.authProvider = authProvider;
        this.permissionService = permissionService;
        this.userService = userService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // enable anon user
        http.
                anonymous();

        // stateless sessions
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // csrf
        http
                .csrf()
                .ignoringAntMatchers(
                        "/api/v1/authenticate",
                        "/api/v1/authenticate/user",
                        "/api/v1/sessions/current",
                        "/api/v1/keys",
                        "/api/sync_sso",
                        "/paypal/ipn"
                );

        // disabled not needed auth stuff
        http
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();

        // auth
        http
                .addFilter(new HangarAuthenticationFilter())
                .exceptionHandling().authenticationEntryPoint(new HangarAuthenticationEntryPoint())
                .and().authorizeRequests().anyRequest().permitAll().accessDecisionManager(accessDecisionManager());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(authProvider);
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
                new ProjectPermissionVoter(permissionService),
                new OrganizationPermissionVoter(permissionService),
                new GlobalPermissionVoter(permissionService),
                new UserLockVoter(userService)
        );
        return new UnanimousBased(decisionVoters);
    }
}
