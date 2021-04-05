package io.papermc.hangar.security.authentication;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.server.ResponseStatusException;

public class HangarAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3847099128940870714L;

    private final DecodedJWT token;
    private final HangarPrincipal user;

    // Used by HangarAuthenticationProvider once user is verified
    public HangarAuthenticationToken(HangarPrincipal user, DecodedJWT token) {
        super(AuthorityUtils.createAuthorityList("ROLE_USER"));
        this.token = token;
        this.user = user;
        super.setAuthenticated(true);
    }

    // Initial token creation before verifying the user exists in the table
    public HangarAuthenticationToken(DecodedJWT token) {
        super(null);
        this.token = token;
        this.user = null;
    }

    @Override
    public DecodedJWT getCredentials() {
        return token;
    }

    @Override
    public String getName() {
        if (user == null) {
            throw new AuthenticationServiceException("This authentication token is not authenticated, so it doesn't have a user");
        }
        return user.getName();
    }

    @Override
    @NotNull
    public HangarPrincipal getPrincipal() {
        if (user == null) {
            throw new AuthenticationServiceException("This authentication token is not authenticated, so it doesn't have a user");
        }
        return user;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    public long getUserId() {
        if (this.user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not using an authenticated HangarAuthenticationToken");
        }
        return this.user.getId();
    }
}
