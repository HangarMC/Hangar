package io.papermc.hangar.security.internal;

import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.service.api.UsersApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class HangarAuthenticationProvider implements AuthenticationProvider {

    private final UsersApiService usersApiService;

    @Autowired
    public HangarAuthenticationProvider(UsersApiService usersApiService) {
        this.usersApiService = usersApiService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof HangarAuthenticationToken)) { // Not sure this is needed since the supports method already checks this?
            return null;
        }

        HangarAuthenticationToken token = (HangarAuthenticationToken) authentication;
        String userName = token.getDetails();
        HangarUser user = usersApiService.getUser(userName, HangarUser.class);

        if (user != null) {
            System.out.println("NOT NULL");
            return new HangarAuthenticationToken(user, token.getCredentials());
        } else {
            System.out.println("NULL");
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(HangarAuthenticationToken.class);
    }
}
