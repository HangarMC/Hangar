package io.papermc.hangar.controller.extras.resolvers;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.users.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

@Component
public class UserTableResolver extends PathVariableMethodArgumentResolver {

    private final UserDAO userDAO;
    private final UserService userService;

    @Autowired
    public UserTableResolver(HangarDao<UserDAO> userDAO, UserService userService) {
        this.userDAO = userDAO.get();
        this.userService = userService;
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return super.supportsParameter(parameter) && parameter.getParameterType().equals(UserTable.class);
    }

    @Override
    protected UserTable resolveName(@NotNull String name, @NotNull MethodParameter parameter, @NotNull NativeWebRequest request) throws Exception {
        String userName = (String) super.resolveName(name, parameter, request);
        if (userName == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (parameter.hasParameterAnnotation(NoCache.class)) {
            return userDAO.getUserTable(userName);
        } else {
            return userService.getUserTable(userName);
        }
    }
}
