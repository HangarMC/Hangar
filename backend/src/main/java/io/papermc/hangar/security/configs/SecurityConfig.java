package io.papermc.hangar.security.configs;

import io.papermc.hangar.security.authentication.HangarAuthenticationFilter;
import io.papermc.hangar.security.authentication.HangarAuthenticationProvider;
import io.papermc.hangar.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String AUTH_NAME = "HangarAuth";
    public static final String REFRESH_COOKIE_NAME = AUTH_NAME + "_REFRESH";

    private static final RequestMatcher API_MATCHER = new AndRequestMatcher(new AntPathRequestMatcher("/api/**"), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/authenticate/**")));
    private static final RequestMatcher PUBLIC_API_MATCHER = new AndRequestMatcher(new AntPathRequestMatcher("/api/v1/**"), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/authenticate/**")));
    private static final RequestMatcher INTERNAL_API_MATCHER = new AntPathRequestMatcher("/api/internal/**");
    private static final RequestMatcher LOGOUT_MATCHER = new AntPathRequestMatcher("/logout");
    private static final RequestMatcher INVALIDATE_MATCHER = new AntPathRequestMatcher("/invalidate");

    private final TokenService tokenService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final HangarAuthenticationProvider authenticationProvider;

    @Autowired
    public SecurityConfig(final TokenService tokenService, final HangarAuthenticationProvider authenticationProvider, final AuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenService = tokenService;
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                // Disable default configurations
                .logout().disable()
                .httpBasic().disable()
                .formLogin().disable()

                // Disable session creation
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                // Disable csrf (shouldn't need it as its just a backend api now)
                .csrf().disable()

                // Custom auth filters
                .addFilterBefore(new HangarAuthenticationFilter(
                        new OrRequestMatcher(API_MATCHER, LOGOUT_MATCHER, INVALIDATE_MATCHER),
                                this.tokenService,
                                this.authenticationManagerBean(),
                                this.authenticationEntryPoint),
                        AnonymousAuthenticationFilter.class
                )

//                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()

                // Permit all (use method security for controller access)
                .authorizeRequests().anyRequest().permitAll();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() {
        return new ProviderManager(this.authenticationProvider);
    }
}
