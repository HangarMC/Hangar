package io.papermc.hangar.security;

import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.service.PermissionService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final HangarDao<UserDao> userDao;
    private final PermissionService permissionService;

    public HangarAuthenticationProvider(HangarDao<UserDao> userDao, PermissionService permissionService) {
        this.userDao = userDao;
        this.permissionService = permissionService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();

        UsersTable usersTable = userDao.get().getByName(userName);
        // TODO validate stuff, guess we need to pass sso stuff here?

        if (usersTable != null) {
            return new HangarAuthentication(List.of(new SimpleGrantedAuthority("ROLE_USER")), userName, usersTable.getId(), usersTable);
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthentication.class);
    }
}
