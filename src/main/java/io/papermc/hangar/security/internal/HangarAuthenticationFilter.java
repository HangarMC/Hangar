package io.papermc.hangar.security.internal;

import io.papermc.hangar.service.TokenService;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
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

    public static final String AUTH_NAME = "HangarAuth";
    private static final String AUTH_TOKEN_ATTR = "HangarAuthJWTToken";

    private final TokenService tokenService;

    public HangarAuthenticationFilter(final RequestMatcher requiresAuth, final TokenService tokenService, final AuthenticationManager authenticationManager) {
        super(requiresAuth);
        this.setAuthenticationManager(authenticationManager);
        this.tokenService = tokenService;
//        this.setAuthenticationSuccessHandler((request, response, authentication) -> request.getRequestDispatcher(request.getRequestURI()).forward(request, response));
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (super.requiresAuthentication(request, response)) {
            Optional<String> token = Stream.of(
                    Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).map(value -> value.replace(AUTH_NAME, "").trim()),
                    Optional.ofNullable(request.getParameter("t")),
                    Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(AUTH_NAME)).map(Cookie::getValue).findFirst()
            ).flatMap(Optional::stream).findFirst();
            if (token.isEmpty()) {
                if (this.logger.isTraceEnabled()) {
                    logger.trace("Couldn't find a " + AUTH_NAME + " token on the request");
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
        // request should ALWAYS have a `jwtToken` attribute here
        String jwt = (String) request.getAttribute(AUTH_TOKEN_ATTR);
        try {
            HangarAuthenticationToken token = new HangarAuthenticationToken(tokenService.verify(jwt));
            return getAuthenticationManager().authenticate(token);
        } catch (Exception e) {
            throw e;
//            throw new BadCredentialsException("Unable to verify the JWT", e);
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
}
