package io.papermc.hangar.security.authentication;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.log.LogMessage;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class HangarAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String AUTH_TOKEN_ATTR = SecurityConfig.AUTH_NAME + "JWTToken";
    private static final Logger logger = LoggerFactory.getLogger(HangarAuthenticationFilter.class);

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
            final Optional<String> token = Stream.of(
                    Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).map(value -> value.replace(SecurityConfig.AUTH_NAME, "").trim()),
                    Optional.ofNullable(request.getParameter("t")),
                    Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies).filter(c -> c.getName().equals(SecurityConfig.AUTH_NAME)).map(Cookie::getValue).findFirst())
            ).flatMap(Optional::stream).findFirst();
            if (token.isEmpty()) {
                logger.trace("Couldn't find a {} token on the request {}", SecurityConfig.AUTH_NAME, request.getRequestURI());
                return false;
            }
            request.setAttribute(AUTH_TOKEN_ATTR, token.get());
            return true;
        }
        return false;
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
        logger.debug("Set SecurityContextHolder to {}", authResult);
        chain.doFilter(request, response);
    }
}
