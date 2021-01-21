package io.papermc.hangar.security;

import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.modelold.UsersTable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final HangarDao<UserDao> userDao;

    public HangarAuthenticationProvider(HangarDao<UserDao> userDao) {
        this.userDao = userDao;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();

        UsersTable usersTable = userDao.get().getByName(userName);

        if (usersTable != null) {
            return new HangarAuthentication(List.of(new SimpleGrantedAuthority("ROLE_USER")), userName, usersTable.getId());
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthentication.class);
    }
}
