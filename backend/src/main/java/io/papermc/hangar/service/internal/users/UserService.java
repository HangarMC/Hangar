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
    public UserService(final UserDAO userDAO, final HangarUsersDAO hangarUsersDAO, @Lazy final RestTemplate restTemplate) {
        this.userDAO = userDAO;
        this.hangarUsersDAO = hangarUsersDAO;
        this.restTemplate = restTemplate;
    }

    public UserTable insertUser(final UserTable userTable) {
        return this.userDAO.insert(userTable);
    }

    public @Nullable UserTable getUserTable(final @Nullable String userName) {
        return this.getUserTable(userName, this.userDAO::getUserTable);
    }

    public @Nullable UserTable getUserTable(final @Nullable Long userId) {
        return this.getUserTable(userId, this.userDAO::getUserTable);
    }

    public void toggleWatching(final long projectId, final boolean state) {
        if (state) {
            this.hangarUsersDAO.setWatching(projectId, this.getHangarPrincipal().getUserId());
        } else {
            this.hangarUsersDAO.setNotWatching(projectId, this.getHangarPrincipal().getUserId());
        }
    }

    public void toggleStarred(final long projectId, final boolean state) {
        if (state) {
            this.hangarUsersDAO.setStarred(projectId, this.getHangarPrincipal().getUserId());
        } else {
            this.hangarUsersDAO.setNotStarred(projectId, this.getHangarPrincipal().getUserId());
        }
    }

    @Transactional
    public void markPromptRead(final Prompt prompt) {
        final UserTable userTable = this.userDAO.getUserTable(this.getHangarPrincipal().getId());
        if (!userTable.getReadPrompts().contains(prompt.ordinal())) {
            userTable.getReadPrompts().add(prompt.ordinal());
            this.userDAO.update(userTable);
        }
    }

    @Transactional
    public void setLocked(final UserTable user, final boolean locked, final String comment) {
        if (user.isLocked() != locked) {
            user.setLocked(locked);
            this.userDAO.update(user);
            final UserContext userContext = UserContext.of(user.getUserId());
            final LoggedAction<UserContext> loggedAction;
            if (locked) {
                loggedAction = LogAction.USER_LOCKED.create(userContext, user.getName() + " is now locked: " + comment, user.getName() + " was unlocked");
            } else {
                loggedAction = LogAction.USER_UNLOCKED.create(userContext, user.getName() + " has been unlocked: " + comment, user.getName() + " was locked");
            }
            this.actionLogger.user(loggedAction);
        }
    }

    public void updateUser(final UserTable userTable) {
        this.userDAO.update(userTable);
    }

    private @Nullable <T> UserTable getUserTable(final @Nullable T identifier, final @NotNull Function<T, UserTable> userTableFunction) {
        if (identifier == null) {
            return null;
        }
        return userTableFunction.apply(identifier);
    }

    public void updateSSO(final UUID uuid, final Traits traits) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<Traits> requestEntity = new HttpEntity<>(traits, headers);

        try {
            final ResponseEntity<Void> response = this.restTemplate.postForEntity(this.config.security.api().url() + "/sync/user/" + uuid.toString() + "?apiKey=" + this.config.sso.apiKey(), requestEntity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(response.getStatusCode(), "Error from auth api");
            }
        } catch (final HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }
}
