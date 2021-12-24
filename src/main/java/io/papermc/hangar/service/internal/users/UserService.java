package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
public class UserService extends HangarComponent {

    private final UserDAO userDAO;
    private final HangarUsersDAO hangarUsersDAO;

    @Autowired
    public UserService(UserDAO userDAO, HangarUsersDAO hangarUsersDAO) {
        this.userDAO = userDAO;
        this.hangarUsersDAO = hangarUsersDAO;
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

    public void toggleWatching(long projectId, boolean state) {
        if (state) {
            hangarUsersDAO.setWatching(projectId, getHangarPrincipal().getUserId());
        } else {
            hangarUsersDAO.setNotWatching(projectId, getHangarPrincipal().getUserId());
        }
    }

    public void toggleStarred(long projectId, boolean state) {
        if (state) {
            hangarUsersDAO.setStarred(projectId, getHangarPrincipal().getUserId());
        } else {
            hangarUsersDAO.setNotStarred(projectId, getHangarPrincipal().getUserId());
        }
    }

    public void markPromptRead(Prompt prompt) {
        UserTable userTable = userDAO.getUserTable(getHangarPrincipal().getId());
        if (!userTable.getReadPrompts().contains(prompt.ordinal())) {
            userTable.getReadPrompts().add(prompt.ordinal());
            userDAO.update(userTable);
        }
    }

    @Transactional
    public void setLocked(UserTable user, boolean locked, String comment) {
        if (user.isLocked() != locked) {
            user.setLocked(locked);
            userDAO.update(user);
            UserContext userContext = UserContext.of(user.getUserId());
            LoggedAction<UserContext> loggedAction;
            if (locked) {
                loggedAction = LogAction.USER_LOCKED.create(userContext, user.getName() + " is now locked: " + comment, user.getName() + " was unlocked");
            } else {
                loggedAction = LogAction.USER_UNLOCKED.create(userContext, user.getName() + " has been unlocked: " + comment, user.getName() + " was locked");
            }
            actionLogger.user(loggedAction);
        }
    }

    public void updateUser(UserTable userTable) {
        userDAO.update(userTable);
    }

    @Nullable
    private <T> UserTable getUserTable(@Nullable T identifier, @NotNull Function<T, UserTable> userTableFunction) {
        if (identifier == null) {
            return null;
        }
        return userTableFunction.apply(identifier);
    }
}
