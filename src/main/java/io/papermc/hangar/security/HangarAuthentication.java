package io.papermc.hangar.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class HangarAuthentication extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3847099128940870714L;

    private final String username;
    private final Long userId;

    public HangarAuthentication(String username) {
        super(null);
        this.username = username;
        this.userId = null;
        setAuthenticated(false);
    }

    public HangarAuthentication(Collection<? extends GrantedAuthority> authorities, String username, long userId) {
        super(authorities);
        this.username = username;
        this.userId = userId;
        super.setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return "";
    }

    @Override
    public String getPrincipal() {
        return this.username;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    public long getUserId() {
        return userId;
    }
}
