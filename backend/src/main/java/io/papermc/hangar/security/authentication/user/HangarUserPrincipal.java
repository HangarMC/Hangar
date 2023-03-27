package io.papermc.hangar.security.authentication.user;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class HangarUserPrincipal extends HangarPrincipal implements UserDetails {

    private final UserTable userTable;
    private final String password;
    private int aal = -1;

    public HangarUserPrincipal(final UserTable userTable, final String password, final Permission globalPermissions) {
        super(userTable.getUserId(), userTable.getName(), userTable.isLocked(), globalPermissions);
        this.userTable = userTable;
        this.password = password;
    }

    public UserTable getUserTable() {
        return this.userTable;
    }

    public int getAal() {
        return this.aal;
    }

    public void setAal(final int aal) {
        this.aal = aal;
    }

    @Override
    public String toString() {
        return "HangarUserPrincipal{" +
            "userTable=" + this.userTable +
            "} " + super.toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("dum"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !this.isLocked();
    }
}
