package io.papermc.hangar.security.internal;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.model.internal.user.HangarUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.server.ResponseStatusException;

public class HangarAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3847099128940870714L;

    private final DecodedJWT token;
    private final HangarUser user;

    // Used by HangarAuthenticationProvider once user is verified
    public HangarAuthenticationToken(HangarUser user, DecodedJWT token) {
        super(AuthorityUtils.createAuthorityList("ROLE_USER"));
        this.token = token;
        this.user = user;
        System.out.println(user);
        super.setAuthenticated(true);
    }

    // Initial token creation before verifying the user exists in the table
    public HangarAuthenticationToken(DecodedJWT token) {
        super(null);
        this.token = token;
        this.user = null;
    }

    @Override
    public String getDetails() {
        return token.getSubject();
    }

    @Override
    public DecodedJWT getCredentials() {
        return token;
    }

    @Override
    public HangarUser getPrincipal() {
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
