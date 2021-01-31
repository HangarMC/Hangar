package io.papermc.hangar.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.papermc.hangar.service.TokenService;

public class HangarAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String AUTH_NAME = "HangarAuth";

    private final TokenService tokenService;

    public HangarAuthenticationFilter(final RequestMatcher requiresAuth, final TokenService tokenService) {
        super(requiresAuth);
        this.tokenService = tokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        final Authentication auth = Stream.of(
                Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).map(value -> value.replace(AUTH_NAME, "").trim()),
                Optional.ofNullable(request.getParameter("t")),
                Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(AUTH_NAME)).map(Cookie::getValue).findFirst()
        ).flatMap(Optional::stream)
                .findFirst()
                .map(tokenService::verify)
                .map(token -> (Authentication) new HangarAuthenticationToken(token))
                .orElseGet(() -> new AnonymousAuthenticationToken("key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        return getAuthenticationManager().authenticate(auth);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
