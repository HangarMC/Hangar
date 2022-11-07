package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.sso.Traits;
import java.util.UUID;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService extends HangarComponent {

    private final UserDAO userDAO;
    private final HangarUsersDAO hangarUsersDAO;
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(UserDAO userDAO, HangarUsersDAO hangarUsersDAO, @Lazy RestTemplate restTemplate) {
        this.userDAO = userDAO;
        this.hangarUsersDAO = hangarUsersDAO;
        this.restTemplate = restTemplate;
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

    @Transactional
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

    public void updateSSO(UUID uuid, Traits traits) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Traits> requestEntity = new HttpEntity<>(traits, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(config.security.api().url() + "/sync/user/" + uuid.toString() + "?apiKey=" + config.sso.apiKey(), requestEntity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(response.getStatusCode(), "Error from auth api");
            }
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }
}
