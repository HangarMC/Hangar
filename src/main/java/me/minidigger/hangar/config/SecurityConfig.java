package me.minidigger.hangar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import me.minidigger.hangar.filter.HangarAuthenticationFilter;
import me.minidigger.hangar.security.HangarAuthenticationProvider;
import me.minidigger.hangar.util.RouteHelper;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final HangarAuthenticationProvider authProvider;
    private final RouteHelper routeHelper;

    @Autowired
    public SecurityConfig(HangarAuthenticationProvider authProvider, RouteHelper routeHelper) {
        this.authProvider = authProvider;
        this.routeHelper = routeHelper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers(
                "/api/v2/authenticate", "/api/v2/sessions/current", "/api/v2/keys"
        );

        http.addFilter(new HangarAuthenticationFilter());

        http.exceptionHandling().authenticationEntryPoint((request, response, e) -> response.sendRedirect(routeHelper.getRouteUrl("users.login", "", "", request.getRequestURI())));

        http.authorizeRequests().anyRequest().permitAll(); // we use method security
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
}
