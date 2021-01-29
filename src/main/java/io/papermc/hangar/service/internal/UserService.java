package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.HangarService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserService extends HangarService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(HangarDao<UserDAO> userDAO) {
        this.userDAO = userDAO.get();
    }

    public UserTable insertUser(UserTable userTable) {
        return userDAO.insert(userTable);
    }

    @Nullable
    public UserTable getUserTable(@Nullable String userName) {
        return getUserTable(userName, userDAO::getUserTable);
    }

    @Nullable
    public UserTable getUserTable(@Nullable Long userId) {
        return getUserTable(userId, userDAO::getUserTable);
    }

    @Nullable
    private <T> UserTable getUserTable(@Nullable T identifier, @NotNull Function<T, UserTable> userTableFunction) {
        if (identifier == null) {
            return null;
        }
        return userTableFunction.apply(identifier);
    }
}
