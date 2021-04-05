package io.papermc.hangar.security.authentication;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.TokenService;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
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

    private final TokenService tokenService;

    public HangarAuthenticationFilter(final RequestMatcher requiresAuth, final TokenService tokenService, final AuthenticationManager authenticationManager) {
        super(requiresAuth);
        this.setAuthenticationManager(authenticationManager);
        this.tokenService = tokenService;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (super.requiresAuthentication(request, response)) {
            Optional<String> token = Stream.of(
                    Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).map(value -> value.replace(SecurityConfig.AUTH_NAME, "").trim()),
                    Optional.ofNullable(request.getParameter("t")),
                    Optional.ofNullable(request.getCookies()).flatMap(cookies -> Arrays.stream(cookies).filter(c -> c.getName().equals(SecurityConfig.AUTH_NAME)).map(Cookie::getValue).findFirst())
            ).flatMap(Optional::stream).findFirst();
            if (token.isEmpty()) {
                if (this.logger.isTraceEnabled()) {
                    logger.trace("Couldn't find a " + SecurityConfig.AUTH_NAME + " token on the request");
                }
                return false;
            }
            request.setAttribute(AUTH_TOKEN_ATTR, token.get());
            return true;
        }
        return false;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // request should ALWAYS have a `HangarAuthJWTToken` attribute here
        String jwt = (String) request.getAttribute(AUTH_TOKEN_ATTR);
        try {
            HangarAuthenticationToken token = new HangarAuthenticationToken(tokenService.verify(jwt));
            return getAuthenticationManager().authenticate(token);
        } catch (TokenExpiredException tokenExpiredException) {
            throw new CredentialsExpiredException("JWT was expired", tokenExpiredException);
        } catch (JWTVerificationException jwtVerificationException) {
            throw new BadCredentialsException("Unable to verify the JWT", jwtVerificationException);
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authResult));
        }
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Failed to process authentication request", failed);
            this.logger.trace("Cleared SecurityContextHolder");
            this.logger.trace("Handling authentication failure");
        }

        HttpStatus status;
        if (failed instanceof CredentialsExpiredException) {
            status = HttpStatus.FORBIDDEN;
        } else if (failed instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        throw new HangarApiException(status, failed.getMessage());
    }
}
