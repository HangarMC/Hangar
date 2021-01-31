package io.papermc.hangar.security;

import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.user.HangarUser;

public class HangarAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3847099128940870714L;

    private final DecodedJWT token;
    private final HangarUser user;

    public HangarAuthenticationToken(HangarUser user, DecodedJWT token) {
        super(AuthorityUtils.createAuthorityList("ROLE_USER"));
        this.token = token;
        this.user = user;
        super.setAuthenticated(true);
    }

    public HangarAuthenticationToken(DecodedJWT token) {
        super(null);
        this.token = token;
        this.user = null;
    }

    @Override
    public HangarUser getDetails() {
        return user;
    }

    @Override
    public DecodedJWT getCredentials() {
        return token;
    }

    @Override
    public String getPrincipal() {
        return this.token.getSubject();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    public long getUserId() {
        return this.user.getId();
    }
}
