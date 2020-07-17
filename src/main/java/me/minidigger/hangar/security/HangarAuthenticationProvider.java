package me.minidigger.hangar.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final HangarDao<UserDao> userDao;

    public HangarAuthenticationProvider(HangarDao<UserDao> userDao) {
        this.userDao = userDao;
    }

    @Override
    public Authentication authenticate(Authentication authentication)  throws AuthenticationException {

        HangarAuthentication auth = (HangarAuthentication) authentication;
        String name = auth.getName();

        UsersTable usersTable = userDao.get().getByName(name);
        // TODO validate stuff, guess we need to pass sso stuff here?

        if (usersTable != null) {
            return new HangarAuthentication(name,usersTable , List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")));
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthentication.class);
    }
}
