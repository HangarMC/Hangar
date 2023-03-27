package io.papermc.hangar.security.configs;

import com.webauthn4j.springframework.security.WebAuthnProcessingFilter;
import io.papermc.hangar.security.authentication.HangarAuthenticationFilter;
import io.papermc.hangar.security.webauthn.WebAuthnConfig;
import io.papermc.hangar.service.TokenService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
    private final WebAuthnConfig webAuthnConfig;

    @Autowired
    public SecurityConfig(final TokenService tokenService, final AuthenticationEntryPoint authenticationEntryPoint, final WebAuthnConfig webAuthnConfig) {
        this.tokenService = tokenService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.webAuthnConfig = webAuthnConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http, final AuthenticationManager authenticationManagerBean) throws Exception {
        this.webAuthnConfig.configure(http, authenticationManagerBean);

        http
            // Disable default configurations
            .logout().disable()
            .httpBasic().disable()
            .formLogin().disable()

            // Disable session creation
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

            // Disable csrf (shouldn't need it as its just a backend api now)
            .csrf().disable()

            // Enable cors
            .cors().and()

            // Custom auth filters
            .addFilterBefore(new HangarAuthenticationFilter(
                    new OrRequestMatcher(API_MATCHER, LOGOUT_MATCHER, INVALIDATE_MATCHER),
                    this.tokenService,
                    authenticationManagerBean,
                    this.authenticationEntryPoint),
                WebAuthnProcessingFilter.class
            )

            // Permit all (use method security for controller access)
            .authorizeRequests().anyRequest().permitAll();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(final UserDetailsService userDetailsService){
        final DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(final List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/api/v1/", config);
        // TODO more cors?
        return new CorsFilter(source);
    }
}
