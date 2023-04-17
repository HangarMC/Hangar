package io.papermc.hangar.security.authentication;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class HangarPrincipal implements ProjectOwner, UserDetails, Serializable {

    private final long id;
    private final String name;
    private final String email;
    private final boolean locked;
    private final Permission globalPermissions;
    private final int aal;
    private final boolean privileged;
    private final String password;

    public HangarPrincipal(final long id, final String name, final String email, final boolean locked, final Permission globalPermissions, final String password, final int aal, final boolean privileged) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.locked = locked;
        this.globalPermissions = globalPermissions;
        this.password = password;
        this.aal = aal;
        this.privileged = privileged;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long getUserId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public final boolean isLocked() {
        return this.locked;
    }

    public Permission getPossiblePermissions() {
        return Permission.All;
    }

    public final Permission getGlobalPermissions() {
        return this.globalPermissions.intersect(this.getPossiblePermissions());
    }

    public int getAal() {
        return this.aal;
    }

    public boolean isPrivileged() {
        return this.privileged;
    }

    public final boolean isAllowedGlobal(final Permission requiredPermission) {
        return this.isAllowed(requiredPermission, this.globalPermissions);
    }

    public final boolean isAllowed(final Permission requiredPermission, final Permission currentPermission) {
        final Permission intersect = requiredPermission.intersect(currentPermission);
        if (intersect.isNone()) {
            return false;
        }
        return this.getPossiblePermissions().has(intersect);
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

    @Override
    public String toString() {
        return "HangarPrincipal{" +
            "id=" + this.id +
            ", name='" + this.name + '\'' +
            ", locked=" + this.locked +
            ", globalPermissions=" + this.globalPermissions +
            ", aal=" + this.aal +
            '}';
    }
}
