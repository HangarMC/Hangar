package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class UserTableResolver extends HangarModelResolver<UserTable> {

    private final UserService userService;

    @Autowired
    public UserTableResolver(final UserService userService) {
        this.userService = userService;
    }

    @Override
    protected Class<UserTable> modelType() {
        return UserTable.class;
    }

    @Override
    protected UserTable resolveParameter(final @NotNull String param, final NativeWebRequest request) {
        UserTable userTable = null;
        if (this.supportsId(request) && StringUtils.isLong(param)) {
            userTable = this.userService.getUserTable(Long.parseLong(param));
        }

        if (userTable == null) {
            userTable = this.userService.getUserTable(param);
        }

        if (userTable != null) {
            request.setAttribute("userId", userTable.getId(), NativeWebRequest.SCOPE_REQUEST);
            return userTable;
        }

        return null;
    }
}
