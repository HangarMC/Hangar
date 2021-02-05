package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.SsoSyncData;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.roles.GlobalRoleService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class UserService extends HangarService {

    private final UserDAO userDAO;
    private final GlobalRoleService globalRoleService;

    @Autowired
    public UserService(HangarDao<UserDAO> userDAO, GlobalRoleService globalRoleService) {
        this.userDAO = userDAO.get();
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
                    null,
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
            globalRoleService.addRole(addGroup.create(null, user.getId(), true));
        }

        for (GlobalRole removeGroup : syncData.getRemoveGroups()) {
            globalRoleService.deleteRole(removeGroup.create(null, user.getId(), true));
        }
    }
}
