package io.papermc.hangar.service;

import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AuthenticationService extends HangarService {

    private final UserService userService;
    private final GlobalRoleService globalRoleService;

    public AuthenticationService(UserService userService, GlobalRoleService globalRoleService) {
        this.userService = userService;
        this.globalRoleService = globalRoleService;
    }

    public UserTable loginAsFakeUser() {
        String userName = hangarConfig.fakeUser.getUsername();
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            userTable = new UserTable(
                    hangarConfig.fakeUser.getId(),
                    hangarConfig.fakeUser.getName(),
                    userName,
                    hangarConfig.fakeUser.getEmail(),
                    List.of(),
                    false,
                    Locale.ENGLISH.toLanguageTag()
            );

            userTable = userService.insertUser(userTable);

            globalRoleService.addRole(GlobalRole.HANGAR_ADMIN.create(null, userTable.getId(), true));
        }
        return userTable;
    }
}
