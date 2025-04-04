package io.papermc.hangar.security.configs;

import dev.samstevens.totp.spring.autoconfigure.TotpAutoConfiguration;
import io.papermc.hangar.components.auth.service.TokenService;
import io.papermc.hangar.security.authentication.HangarAuthenticationFilter;
import io.papermc.hangar.security.authentication.HangarAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.*;

@Configuration
@EnableWebSecurity
@ImportAutoConfiguration(TotpAutoConfiguration.class)
public class SecurityConfig {

    public static final String AUTH_NAME = "HangarAuth";
    public static final String REFRESH_COOKIE_NAME = AUTH_NAME + "_REFRESH";

    private static final RequestMatcher API_MATCHER = new AndRequestMatcher(new AntPathRequestMatcher("/api/**"), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/authenticate/**")), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/internal/auth/refresh")));
    private static final RequestMatcher PUBLIC_API_MATCHER = new AndRequestMatcher(new AntPathRequestMatcher("/api/v1/**"), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/authenticate/**")));
    private static final RequestMatcher INTERNAL_API_MATCHER = new AntPathRequestMatcher("/api/internal/**");
    private static final RequestMatcher LOGOUT_MATCHER = new AntPathRequestMatcher("/logout");
    private static final RequestMatcher INVALIDATE_MATCHER = new AntPathRequestMatcher("/invalidate");

    private final TokenService tokenService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final HangarAuthenticationProvider authenticationProvider;

    @Autowired
    public SecurityConfig(final TokenService tokenService, final AuthenticationEntryPoint authenticationEntryPoint, final HangarAuthenticationProvider authenticationProvider) {
        this.tokenService = tokenService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http, final AuthenticationManager authenticationManagerBean) throws Exception {
        http
            // Disable default configurations
            .logout(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)

            // Disable session creation
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Disable csrf (shouldn't need it as its just a backend api now)
            .csrf(AbstractHttpConfigurer::disable)

            // Enable cors
            .cors(withDefaults())

            // Custom auth filters
            .addFilterBefore(new HangarAuthenticationFilter(
                    new OrRequestMatcher(API_MATCHER, LOGOUT_MATCHER, INVALIDATE_MATCHER),
                    this.tokenService,
                    authenticationManagerBean,
                    this.authenticationEntryPoint),
                AnonymousAuthenticationFilter.class
            )

            // Permit all (use method security for controller access)
            .authorizeHttpRequests(s-> s.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() {
        return new ProviderManager(this.authenticationProvider);
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/api/v1/", config);
        // TODO more cors?
        return source;
    }

    @Bean
    public HttpFirewall getHttpFirewall() {
        final StrictHttpFirewall strictHttpFirewall = new StrictHttpFirewall();
        strictHttpFirewall.setAllowUrlEncodedPercent(true);
        strictHttpFirewall.setAllowUrlEncodedSlash(true);
        strictHttpFirewall.setAllowUrlEncodedDoubleSlash(true);
        return strictHttpFirewall;
    }
}
