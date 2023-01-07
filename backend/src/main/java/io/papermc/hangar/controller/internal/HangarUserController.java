package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.UserSettings;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.sso.Traits;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.currentuser.CurrentUser;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.users.invites.InviteService;
import io.papermc.hangar.service.internal.users.invites.OrganizationInviteService;
import io.papermc.hangar.service.internal.users.invites.ProjectInviteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@LoggedIn
@RateLimit(path = "hangaruser")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
public class HangarUserController extends HangarComponent {

    private final ObjectMapper mapper;
    private final UsersApiService usersApiService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ProjectRoleService projectRoleService;
    private final OrganizationRoleService organizationRoleService;
    private final ProjectInviteService projectInviteService;
    private final OrganizationInviteService organizationInviteService;
    private final TokenService tokenService;

    @Autowired
    public HangarUserController(final ObjectMapper mapper, final UsersApiService usersApiService, final UserService userService, final NotificationService notificationService, final ProjectRoleService projectRoleService, final OrganizationRoleService organizationRoleService, final ProjectInviteService projectInviteService, final OrganizationInviteService organizationInviteService, final TokenService tokenService) {
        this.mapper = mapper;
        this.usersApiService = usersApiService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.projectRoleService = projectRoleService;
        this.organizationRoleService = organizationRoleService;
        this.projectInviteService = projectInviteService;
        this.organizationInviteService = organizationInviteService;
        this.tokenService = tokenService;
    }

    @Anyone
    @GetMapping("/users/@me")
    public ResponseEntity<?> getCurrentUser(final HangarAuthenticationToken hangarAuthenticationToken, @CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        final String token;
        final String name;
        if (hangarAuthenticationToken == null) {
            // if we don't have a token, lets see if we can get one via our refresh token
            if (refreshToken == null) {
                // neither token nor refresh token -> sorry no content
                return ResponseEntity.noContent().build();
            }
            try {
                final TokenService.RefreshResponse refreshResponse = this.tokenService.refreshAccessToken(refreshToken);
                token = refreshResponse.accessToken();
                name = refreshResponse.userTable().getName();
            } catch (final HangarApiException ex) {
                // no token + no valid refresh token -> no content
                System.out.println("getCurrentUser failed: " + ex.getMessage());
                return ResponseEntity.noContent().build();
            }
        } else {
            // when we have a token, just use that
            token = hangarAuthenticationToken.getCredentials().getToken();
            name = hangarAuthenticationToken.getName();
        }
        // get user
        final HangarUser user = this.usersApiService.getUser(name, HangarUser.class);
        user.setAccessToken(token);
        return ResponseEntity.ok(user);
    }

    // @el(userName: String)
    @Unlocked
    @CurrentUser("#userName")
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping(path = "/users/{userName}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable final String userName, @RequestBody @Valid final StringContent content) {
        final UserTable userTable = this.userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (content.getContent().length() > this.config.user.maxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }
        final String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(content.getContent());
        this.userService.updateUser(userTable);
        this.actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), userTable.getTagline(), oldTagline));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping("/users/{userName}/settings/resetTagline")
    public void resetTagline(@PathVariable final String userName) {
        final UserTable userTable = this.userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        final String oldTagline = userTable.getTagline();
        if (oldTagline == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
        userTable.setTagline(null);
        this.userService.updateUser(userTable);
        this.actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), "", oldTagline));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 2)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping("/users/{userName}/settings/")
    public void saveSettings(@PathVariable final String userName, @RequestBody final UserSettings settings, final HttpServletResponse response) {
        final UserTable userTable = this.userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        // if nothing changed, then nothing changed!
        if (userTable.getLanguage().equals(settings.getLanguage()) && userTable.getTheme().equals(settings.getTheme())) {
            setThemeCookie(settings, response);
            return;
        }

        userTable.setLanguage(settings.getLanguage());
        userTable.setTheme(settings.getTheme());
        // TODO user action logging
        this.userService.updateUser(userTable);

        if (this.config.sso.enabled()) {
            this.userService.updateSSO(userTable.getUuid(), new Traits(userTable.getEmail(), null, null, settings.getLanguage(), userTable.getName(), settings.getTheme()));
        }

        setThemeCookie(settings, response);
    }

    @Anyone
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 2)
    @PostMapping("/users/anon/settings/")
    public void saveSettings(@RequestBody final UserSettings settings, final HttpServletResponse response) {
        setThemeCookie(settings, response);
    }

    private static void setThemeCookie(final UserSettings settings, final HttpServletResponse response) {
        final Cookie cookie = new Cookie("HANGAR_theme", settings.getTheme());
        cookie.setPath("/");
        cookie.setMaxAge((int) (60 * 60 * 24 * 356.24 * 1000));
        // TODO make sure this cookie is cross hangar and auth
        response.addCookie(cookie);
    }

    @GetMapping("/recentnotifications")
    public ResponseEntity<List<HangarNotification>> getRecentNotifications(@RequestParam final int amount) {
        return ResponseEntity.ok(this.notificationService.getRecentNotifications(amount));
    }

    @GetMapping(path = "/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResult<HangarNotification>> getNotifications(final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.notificationService.getNotifications(pagination, null));
    }

    @GetMapping(path = "/readnotifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResult<HangarNotification>> getReadNotifications(final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.notificationService.getNotifications(pagination, true));
    }

    @GetMapping(path = "/unreadnotifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResult<HangarNotification>> getUnreadNotifications(final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.notificationService.getNotifications(pagination, false));
    }

    @GetMapping("/unreadcount")
    public ResponseEntity<Long> getUnreadNotifications() {
        return ResponseEntity.ok(this.notificationService.getUnreadNotifications());
    }

    @Unlocked
    @PostMapping("/markallread")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markNotificationAsRead() {
        this.notificationService.markNotificationsAsRead();
    }

    @Unlocked
    @PostMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markNotificationAsRead(@PathVariable final long id) {
        if (!this.notificationService.markNotificationAsRead(id)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/invites")
    public ResponseEntity<ObjectNode> getUserInvites() {
        final ObjectNode invites = this.mapper.createObjectNode();
        invites.set(HangarInvite.InviteType.PROJECT.toString(), this.mapper.valueToTree(this.projectInviteService.getProjectInvites()));
        invites.set(HangarInvite.InviteType.ORGANIZATION.toString(), this.mapper.valueToTree(this.organizationInviteService.getOrganizationInvites()));
        return ResponseEntity.ok(invites);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/read-prompt/{prompt}")
    public void readPrompt(@PathVariable final Prompt prompt) {
        this.userService.markPromptRead(prompt);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/invites/project/{id}/{status}")
    public void updateProjectInviteStatus(@PathVariable final long id, @PathVariable final InviteStatus status) {
        this.updateRole(this.projectRoleService, this.projectInviteService, id, status);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/invites/organization/{id}/{status}")
    public void updateOrganizationInviteStatus(@PathVariable final long id, @PathVariable final InviteStatus status) {
        this.updateRole(this.organizationRoleService, this.organizationInviteService, id, status);
    }

    private <RT extends ExtendedRoleTable<? extends Role<RT>, ?>, RS extends RoleService<RT, ?, ?>, IS extends InviteService<?, ?, RT, ?>> void updateRole(final RS roleService, final IS inviteService, final long id, final InviteStatus status) {
        final RT table = roleService.getRole(id);
        if (table == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        switch (status) {
            case DECLINE -> inviteService.declineInvite(table);
            case ACCEPT -> inviteService.acceptInvite(table);
        }
    }

    public enum InviteStatus {
        ACCEPT,
        DECLINE
    }
}
