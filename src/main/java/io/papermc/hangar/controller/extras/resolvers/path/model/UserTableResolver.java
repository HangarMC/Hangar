package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.users.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTableResolver extends HangarModelPathVarResolver<UserTable> {

    private final UserService userService;

    @Autowired
    public UserTableResolver(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected Class<UserTable> modelType() {
        return UserTable.class;
    }

    @Override
    protected UserTable resolveParameter(@NotNull String param) {
        return userService.getUserTable(param);
    }
}
