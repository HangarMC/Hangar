package io.papermc.hangar.service.internal.auth;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HangarUserDetailService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public HangarUserDetailService(final PasswordEncoder passwordEncoder, final UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Override
    public HangarPrincipal loadUserByUsername(final String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("no user with null username");
        }
        System.out.println("loading user " + username);
        final UserTable userTable = this.userService.getUserTable(username);
        if (userTable == null) {
            throw new UsernameNotFoundException("no user in table");
        }
        // TODO store PW in db properly
        // TODO load proper perms
        return new HangarPrincipal(userTable.getUserId(), userTable.getName(), userTable.isLocked(), Permission.ViewPublicInfo, this.passwordEncoder.encode("admin123"));
    }
}
