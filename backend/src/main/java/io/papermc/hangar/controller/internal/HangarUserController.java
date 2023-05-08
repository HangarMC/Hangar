package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.service.CredentialsService;
import io.papermc.hangar.components.auth.service.TokenService;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.stats.ProjectViewsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.UserProfileSettings;
import io.papermc.hangar.model.internal.api.requests.UserSettings;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
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
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
@LoggedIn
@RateLimit(path = "hangaruser")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
public class HangarUserController extends HangarComponent {

    private static final Set<String> ACCEPTED_SOCIAL_TYPES = Set.of("discord", "github");

    private final ObjectMapper mapper;
    private final UsersApiService usersApiService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ProjectRoleService projectRoleService;
    private final OrganizationService organizationService;
    private final OrganizationRoleService organizationRoleService;
    private final ProjectInviteService projectInviteService;
    private final OrganizationInviteService organizationInviteService;
    private final TokenService tokenService;
    private final AvatarService avatarService;
    private final CredentialsService credentialsService;
    private final ProjectViewsDAO projectViewsDAO;

    @Autowired
    public HangarUserController(final ObjectMapper mapper, final UsersApiService usersApiService, final UserService userService, final NotificationService notificationService, final ProjectRoleService projectRoleService, final OrganizationService organizationService, final OrganizationRoleService organizationRoleService, final ProjectInviteService projectInviteService, final OrganizationInviteService organizationInviteService, final TokenService tokenService, final AvatarService avatarService, final CredentialsService credentialsService, final ProjectViewsDAO projectViewsDAO) {
        this.mapper = mapper;
        this.usersApiService = usersApiService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.projectRoleService = projectRoleService;
        this.organizationService = organizationService;
        this.organizationRoleService = organizationRoleService;
        this.projectInviteService = projectInviteService;
        this.organizationInviteService = organizationInviteService;
        this.tokenService = tokenService;
        this.avatarService = avatarService;
        this.credentialsService = credentialsService;
        this.projectViewsDAO = projectViewsDAO;
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
        user.setAal(this.credentialsService.getAal(Objects.requireNonNull(this.userService.getUserTable(user.getId()))));
        return ResponseEntity.ok(user);
    }

    @Unlocked
    @PermissionRequired(NamedPermission.IS_STAFF)
    @ResponseBody
    @GetMapping(value = "/users/{userName}/alts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> possibleAltAccounts(@PathVariable final String userName) {
        final UserTable table = this.userService.getUserTable(userName);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown org " + userName);
        }
        return this.projectViewsDAO.getUsersSharingAddressWith(table.getUserId());
    }

    // @el(userName: String)
    @Unlocked
    @CurrentUser("#userName")
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping(path = "/users/{userName}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable final String userName, @RequestBody final StringContent content) {
        final String s = content.contentOrEmpty();
        if (s.length() > this.config.user.maxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }

        final UserTable userTable = this.userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(s);
        this.userService.updateUser(userTable);
        this.actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), userTable.getTagline(), oldTagline));
    }

    // @el(userName: String)
    @Unlocked
    @CurrentUser("#userName")
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

    // @el(userName: String)
    @Unlocked
    @CurrentUser("#userName")
    @ResponseBody
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping(value = "/users/{userName}/settings/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void changeAvatar(@PathVariable final String userName, @RequestParam final MultipartFile avatar) throws IOException {
        final UserTable table = this.userService.getUserTable(userName);
        if (table == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown org " + userName);
        }
        this.avatarService.changeUserAvatar(table.getUuid(), avatar.getBytes());
    }

    // @el(userName: String)
    @Unlocked
    @CurrentUser("#userName")
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
        if (Objects.equals(userTable.getLanguage(), settings.getLanguage()) && Objects.equals(userTable.getTheme(), settings.getTheme())) {
            setThemeCookie(settings, response);
            return;
        }

        userTable.setLanguage(settings.getLanguage());
        userTable.setTheme(settings.getTheme());
        // TODO user action logging
        this.userService.updateUser(userTable);

        setThemeCookie(settings, response);
    }

    // @el(userName: String)
    @Unlocked
    @CurrentUser("#userName")
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 2)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping("/users/{userName}/settings/profile")
    public void saveProfileSettings(@PathVariable final String userName, @RequestBody final UserProfileSettings settings) {
        final UserTable userTable = this.userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        Map<String, String> map = new HashMap<>();
        for (final String[] social : settings.socials()) {
            if (social.length != 2) {
                throw new HangarApiException("Badly formatted request, " + Arrays.toString(social) + " wasn't of length 2!");
            }
            if (!ACCEPTED_SOCIAL_TYPES.contains(social[0])) {
                throw new HangarApiException("Badly formatted request, social type " + social[0] + " is unknown!");
            }
            map.put(social[0], social[1]);
        }
        userTable.setSocials(new JSONB(map));
        userTable.setTagline(settings.tagline());
        // TODO user action logging
        this.userService.updateUser(userTable);
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

        final OrganizationTable organizationTable = this.organizationService.getOrganizationTableByUser(table.getUserId());
        final long notificationReceiver = organizationTable != null ? organizationTable.getOwnerId() : table.getUserId();
        if (notificationReceiver != this.getHangarPrincipal().getId()) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        if (status == InviteStatus.DECLINE) {
            inviteService.declineInvite(table);
        } else if (status == InviteStatus.ACCEPT) {
            inviteService.acceptInvite(table);
        }
    }

    public enum InviteStatus {
        ACCEPT,
        DECLINE
    }
}
