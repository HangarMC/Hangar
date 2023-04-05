package io.papermc.hangar.security.authentication;

import io.papermc.hangar.components.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final TokenService tokenService;

    @Autowired
    public HangarAuthenticationProvider(final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final HangarAuthenticationToken token = (HangarAuthenticationToken) authentication;

        final HangarPrincipal hangarPrincipal = this.tokenService.parseHangarPrincipal(token.getCredentials());
        return HangarAuthenticationToken.createVerifiedToken(hangarPrincipal, token.getCredentials());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(HangarAuthenticationToken.class);
    }
}
