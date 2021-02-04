package io.papermc.hangar.security.configs;

import io.papermc.hangar.security.HangarAuthenticationEntryPoint;
import io.papermc.hangar.security.HangarAuthenticationFilter;
import io.papermc.hangar.security.HangarAuthenticationProvider;
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

    public static final String AUTH_NAME = "HangarAuth";
    public static final String AUTH_NAME_REFRESH_COOKIE = AUTH_NAME + "_REFRESH";

    private static final RequestMatcher API_MATCHER = new AndRequestMatcher(new AntPathRequestMatcher("/api/**"), new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/authenticate/**")));
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

                // Disable csrf (shouldn't need it as its just a backend api now)
                .csrf().disable()

                // Custom auth filters
                .addFilterBefore(new HangarAuthenticationFilter(API_MATCHER, tokenService, authenticationManager()), AnonymousAuthenticationFilter.class)

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
