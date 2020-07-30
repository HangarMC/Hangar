package me.minidigger.hangar.security;

import me.minidigger.hangar.db.model.UsersTable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class HangarAuthentication extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3847099128940870714L;

    private final String username;
    private UsersTable table; // todo replace me

    public HangarAuthentication(String username) {
        super(null);
        this.username = username;
        setAuthenticated(false);
    }

    public HangarAuthentication(String username, UsersTable usersTable, Collection<GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.table = usersTable;
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public String getCredentials() {
        return "";
    }

    @Override
    public String getPrincipal() {
        return this.username;
    }

    public UsersTable getTable() {
        return table;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    public long getUserId() {
        return table.getId();
    }
}
