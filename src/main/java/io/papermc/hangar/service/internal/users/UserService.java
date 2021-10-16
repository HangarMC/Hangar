package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.SsoSyncData;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
public class UserService extends HangarComponent {

    private final UserDAO userDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final GlobalRoleService globalRoleService;

    @Autowired
    public UserService(UserDAO userDAO, HangarUsersDAO hangarUsersDAO, GlobalRoleService globalRoleService) {
        this.userDAO = userDAO;
        this.hangarUsersDAO = hangarUsersDAO;
        this.globalRoleService = globalRoleService;
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

    public UserTable getOrCreate(String userName, AuthUser authUser) {
        UserTable user = getUserTable(userName);
        if (user == null) {
            user = new UserTable(
                    authUser.getId(),
                    authUser.getFullName(),
                    authUser.getUserName(),
                    authUser.getEmail(),
                    List.of(),
                    false,
                    authUser.getLang().toLanguageTag()
            );
            user = insertUser(user);
        }
        return user;
    }

    public void ssoSyncUser(SsoSyncData syncData) {
        UserTable user = userDAO.getUserTable(syncData.getExternalId());
        if (user == null) {
            user = new UserTable(
                    syncData.getExternalId(),
                    syncData.getFullName(),
                    syncData.getUsername(),
                    syncData.getEmail(),
                    List.of(),
                    false,
                    null
            );
            user = userDAO.insert(user);
        } else {
            user.setFullName(syncData.getFullName());
            user.setName(syncData.getUsername());
            user.setEmail(syncData.getEmail());
            userDAO.update(user);
        }

        for (GlobalRole addGroup : syncData.getAddGroups()) {
            globalRoleService.addRole(addGroup.create(null, user.getId(), true), true);
        }

        for (GlobalRole removeGroup : syncData.getRemoveGroups()) {
            globalRoleService.deleteRole(removeGroup.create(null, user.getId(), true));
        }
    }
}
