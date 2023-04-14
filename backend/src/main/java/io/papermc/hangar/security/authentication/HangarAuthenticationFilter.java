package io.papermc.hangar.security.authentication;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.components.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class HangarAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String AUTH_TOKEN_ATTR = SecurityConfig.AUTH_NAME + "JWTToken";
    private static final Logger LOGGER = LoggerFactory.getLogger(HangarAuthenticationFilter.class);

    private final TokenService tokenService;

    public HangarAuthenticationFilter(final RequestMatcher requiresAuth, final TokenService tokenService, final AuthenticationManager authenticationManager, final AuthenticationEntryPoint authenticationEntryPoint) {
        super(requiresAuth);
        this.setAuthenticationManager(authenticationManager);
        this.setAuthenticationFailureHandler(new AuthenticationEntryPointFailureHandler(authenticationEntryPoint));
        this.tokenService = tokenService;
    }

    @Override
    protected boolean requiresAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        if (super.requiresAuthentication(request, response)) {
            final String token = this.token(request);
            if (token == null) {
                LOGGER.trace("Couldn't find a {} token on the request {}", SecurityConfig.AUTH_NAME, request.getRequestURI());
                return false;
            }
            request.setAttribute(AUTH_TOKEN_ATTR, token);
            return true;
        }
        return false;
    }

    private @Nullable String token(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null) {
            return authorizationHeader.replace(SecurityConfig.AUTH_NAME, "").replace("Bearer", "").trim();
        }

        final String parameter = request.getParameter("t");
        if (parameter != null) {
            return parameter;
        }

        return Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies).filter(c -> c.getName().equals(SecurityConfig.AUTH_NAME)).map(Cookie::getValue).findFirst()).orElse(null);
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // request should ALWAYS have a `HangarAuthJWTToken` attribute here
        final String jwt = (String) request.getAttribute(AUTH_TOKEN_ATTR);
        try {
            final HangarAuthenticationToken token = HangarAuthenticationToken.createUnverifiedToken(this.tokenService.verify(jwt));
            return this.getAuthenticationManager().authenticate(token);
        } catch (final TokenExpiredException tokenExpiredException) {
            throw new CredentialsExpiredException("JWT was expired", tokenExpiredException);
        } catch (final JWTVerificationException jwtVerificationException) {
            throw new BadCredentialsException("Unable to verify the JWT", jwtVerificationException);
        } catch (final Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        LOGGER.debug("Set SecurityContextHolder to {}", authResult);
        chain.doFilter(request, response);
    }
}
