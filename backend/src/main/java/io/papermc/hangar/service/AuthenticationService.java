package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService extends HangarComponent {

    private final UserService userService;
    private final GlobalRoleService globalRoleService;

    public AuthenticationService(final UserService userService, final GlobalRoleService globalRoleService, final RestTemplate restTemplate) {
        this.userService = userService;
        this.globalRoleService = globalRoleService;
    }

    public UserTable loginAsFakeUser() {
        final String userName = this.config.fakeUser.username();
        UserTable userTable = this.userService.getUserTable(userName);
        if (userTable == null) {
            userTable = new UserTable(
                -1, // we can pass -1 here since it's not actually inserted in the DB in the DAO
                UUID.randomUUID(),
                userName,
                this.config.fakeUser.email(),
                List.of(),
                false,
                Locale.ENGLISH.toLanguageTag(),
                "white"
            );

            userTable = this.userService.insertUser(userTable);

            this.globalRoleService.addRole(GlobalRole.HANGAR_ADMIN.create(null, userTable.getUuid(), userTable.getId(), true));
        }
        return userTable;
    }
}
