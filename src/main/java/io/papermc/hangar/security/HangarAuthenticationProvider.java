package io.papermc.hangar.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UsersDAO;
import io.papermc.hangar.model.internal.user.HangarUser;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final HangarDao<UsersDAO> userDao;

    public HangarAuthenticationProvider(HangarDao<UsersDAO> userDao) {
        this.userDao = userDao;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof HangarAuthenticationToken)) {
            return null;
        }

        HangarAuthenticationToken token = (HangarAuthenticationToken) authentication;
        String userName = token.getName();

        HangarUser user = userDao.get().getUser((userName, HangarUser.class);

        if (user != null) {
            return new HangarAuthenticationToken(user, token.getCredentials());
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthenticationToken.class);
    }
}
