package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarUsersDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends HangarComponent {

    private final UserDAO userDAO;
    private final HangarUsersDAO hangarUsersDAO;

    private static final Set<String> ACCEPTED_SOCIAL_TYPES = Set.of("discord", "github", "twitter", "youtube", "website");

    @Autowired
    public UserService(final UserDAO userDAO, final HangarUsersDAO hangarUsersDAO) {
        this.userDAO = userDAO;
        this.hangarUsersDAO = hangarUsersDAO;
    }

    public UserTable insertUser(final UserTable userTable) {
        return this.userDAO.insert(userTable);
    }

    public @Nullable UserTable getUserTable(final @Nullable String userName) {
        return this.getUserTable(userName, this.userDAO::getUserTable);
    }

    public @Nullable UserTable getUserTable(final UUID uuid) {
        return this.getUserTable(uuid, this.userDAO::getUserTable);
    }

    public @Nullable UserTable getUserTable(final @Nullable Long userId) {
        return this.getUserTable(userId, this.userDAO::getUserTable);
    }

    public void toggleWatching(final long userId, final long projectId, final boolean state) {
        if (state) {
            this.hangarUsersDAO.setWatching(projectId, userId);
        } else {
            this.hangarUsersDAO.setNotWatching(projectId, userId);
        }
    }

    public void toggleStarred(final long userId, final long projectId, final boolean state) {
        if (state) {
            this.hangarUsersDAO.setStarred(projectId, userId);
        } else {
            this.hangarUsersDAO.setNotStarred(projectId, userId);
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

    public void validateSocials(Map<String, String> socials) {
        for (final Map.Entry<String, String> social : socials.entrySet()) {
            if (!ACCEPTED_SOCIAL_TYPES.contains(social.getKey())) {
                throw new HangarApiException("Badly formatted request, social type " + social.getKey() + " is unknown!");
            }
            if ("website".equals(social.getKey()) && !social.getValue().matches(this.config.getUrlRegex())) {
                throw new HangarApiException("Badly formatted request, website " + social.getValue() + " is not a valid url! (Did you add https://?)");
            }
        }
    }

    private @Nullable <T> UserTable getUserTable(final @Nullable T identifier, final @NotNull Function<T, UserTable> userTableFunction) {
        if (identifier == null) {
            return null;
        }
        return userTableFunction.apply(identifier);
    }

    public void delete(final UserTable user) {
        this.userDAO.delete(user.getUserId());
    }
}
