package io.papermc.hangar.security.authentication;

import io.papermc.hangar.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final TokenService tokenService;

    @Autowired
    public HangarAuthenticationProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        HangarAuthenticationToken token = (HangarAuthenticationToken) authentication;

        HangarPrincipal hangarPrincipal = tokenService.parseHangarPrincipal(token.getCredentials());
        return HangarAuthenticationToken.createVerifiedToken(hangarPrincipal, token.getCredentials());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthenticationToken.class);
    }
}
