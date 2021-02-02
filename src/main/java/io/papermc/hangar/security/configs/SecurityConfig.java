package io.papermc.hangar.security.configs;

import io.papermc.hangar.security.entrypoints.HangarAuthenticationEntryPoint;
import io.papermc.hangar.security.internal.HangarAuthenticationFilter;
import io.papermc.hangar.security.internal.HangarAuthenticationProvider;
import io.papermc.hangar.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final RequestMatcher PUBLIC_API_MATCHER = new AndRequestMatcher(new AntPathRequestMatcher("/api/v1/**"), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/authenticate/**")));
    private static final RequestMatcher INTERNAL_API_MATCHER = new AntPathRequestMatcher("/api/internal/**");

    private final TokenService tokenService;

    @Autowired
    public SecurityConfig(TokenService tokenService, HangarAuthenticationProvider hangarAuthenticationProvider) {
        this.tokenService = tokenService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // Disable default configurations
                .logout().disable()
//                .httpBasic().disable()
//                .formLogin().disable()

                // Disable session creation
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                // Enable cors except on public API and certain other routes
                .csrf().ignoringAntMatchers("/api/v1/**", "/api/sync_sso", "/paypal/ipn").and()

                // Custom auth filters
                .addFilterBefore(new HangarAuthenticationFilter(INTERNAL_API_MATCHER, tokenService, authenticationManager()), AnonymousAuthenticationFilter.class)

                .exceptionHandling().authenticationEntryPoint(new HangarAuthenticationEntryPoint()).and()

                // Permit all (use method security for controller access)
                .authorizeRequests().anyRequest().permitAll();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
