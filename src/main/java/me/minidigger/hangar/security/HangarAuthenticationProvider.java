package me.minidigger.hangar.security;

import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.security.authorities.PermissionAuthority;
import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.RoleService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final HangarDao<UserDao> userDao;
    private final PermissionService permissionService;

    public HangarAuthenticationProvider(HangarDao<UserDao> userDao, PermissionService permissionService) {
        this.userDao = userDao;
        this.permissionService = permissionService;
    }

    @Override
    public Authentication authenticate(Authentication authentication)  throws AuthenticationException {

        HangarAuthentication auth = (HangarAuthentication) authentication;
        String name = auth.getName();

        UsersTable usersTable = userDao.get().getByName(name);
        // TODO validate stuff, guess we need to pass sso stuff here?

        Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")));

        if (usersTable != null) {
            return new HangarAuthentication(name, usersTable, authorities);
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthentication.class);
    }
}
