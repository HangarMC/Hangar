package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.UserSettings;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.model.internal.user.notifications.HangarInvite.InviteType;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.currentuser.CurrentUser;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.authentication.HangarAuthenticationToken;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.perms.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.users.invites.InviteService;
import io.papermc.hangar.service.internal.users.invites.OrganizationInviteService;
import io.papermc.hangar.service.internal.users.invites.ProjectInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;

@Controller
@LoggedIn
@RateLimit(path = "hangaruser")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET, RequestMethod.POST })
public class HangarUserController extends HangarComponent {

    private final ObjectMapper mapper;
    private final UsersApiService usersApiService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ProjectRoleService projectRoleService;
    private final OrganizationRoleService organizationRoleService;
    private final ProjectInviteService projectInviteService;
    private final OrganizationInviteService organizationInviteService;

    @Autowired
    public HangarUserController(ObjectMapper mapper, UsersApiService usersApiService, UserService userService, NotificationService notificationService, ProjectRoleService projectRoleService, OrganizationRoleService organizationRoleService, ProjectInviteService projectInviteService, OrganizationInviteService organizationInviteService) {
        this.mapper = mapper;
        this.usersApiService = usersApiService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.projectRoleService = projectRoleService;
        this.organizationRoleService = organizationRoleService;
        this.projectInviteService = projectInviteService;
        this.organizationInviteService = organizationInviteService;
    }

    @GetMapping("/users/@me")
    public ResponseEntity<HangarUser> getCurrentUser(HangarAuthenticationToken hangarAuthenticationToken) {
        return ResponseEntity.ok(usersApiService.getUser(hangarAuthenticationToken.getName(), HangarUser.class));
    }

    @Unlocked
    @CurrentUser("#userName")
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 20)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping(path = "/users/{userName}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable String userName, @Valid @RequestBody StringContent content) {
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (content.getContent().length() > config.user.getMaxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }
        String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(content.getContent());
        userService.updateUser(userTable);
        actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), userTable.getTagline(), oldTagline));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping("/users/{userName}/settings/resetTagline")
    public void resetTagline(@PathVariable String userName) {
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        String oldTagline = userTable.getTagline();
        if (oldTagline == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
        userTable.setTagline(null);
        userService.updateUser(userTable);
        actionLogger.user(LogAction.USER_TAGLINE_CHANGED.create(UserContext.of(userTable.getId()), "", oldTagline));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 2)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping("/users/{userName}/settings/")
    public void saveSettings(@PathVariable String userName, @RequestBody UserSettings settings) {
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        // TODO do we need to sync these back to sso?
        userTable.setLanguage(settings.getLanguage());
        userTable.setTheme(settings.getTheme());
        userService.updateUser(userTable);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<HangarNotification>> getUserNotifications(@RequestParam(required = false) Integer amount) {
        return ResponseEntity.ok(notificationService.getUsersNotifications(amount != null ? amount : Integer.MAX_VALUE));
    }

    @GetMapping("/unread")
    public ResponseEntity<Long> getUnreadNotifications() {
        return ResponseEntity.ok(notificationService.getUnreadNotifications());
    }

    @Unlocked
    @PostMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markNotificationAsRead(@PathVariable long id) {
        if (!notificationService.markNotificationAsRead(id)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/invites")
    public ResponseEntity<ObjectNode> getUserInvites() {
        ObjectNode invites = mapper.createObjectNode();
        invites.set(InviteType.PROJECT.toString(), mapper.valueToTree(projectInviteService.getProjectInvites()));
        invites.set(InviteType.ORGANIZATION.toString(), mapper.valueToTree(organizationInviteService.getOrganizationInvites()));
        return ResponseEntity.ok(invites);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/read-prompt/{prompt}")
    public void readPrompt(@PathVariable Prompt prompt) {
        userService.markPromptRead(prompt);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/invites/project/{id}/{status}")
    public void updateProjectInviteStatus(@PathVariable long id, @PathVariable InviteStatus status) {
        updateRole(projectRoleService, projectInviteService, id, status);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/invites/organization/{id}/{status}")
    public void updateOrganizationInviteStatus(@PathVariable long id, @PathVariable InviteStatus status) {
        updateRole(organizationRoleService, organizationInviteService, id, status);
    }

    private <RT extends ExtendedRoleTable<? extends Role<RT>, ?>, RS extends RoleService<RT, ?, ?>, IS extends InviteService<?, ?, RT, ?>> void updateRole(RS roleService, IS inviteService, long id, InviteStatus status) {
        RT table = roleService.getRole(id);
        if (table == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        switch (status) {
            case DECLINE -> inviteService.declineInvite(table);
            case ACCEPT -> inviteService.acceptInvite(table);
            case UNACCEPT -> inviteService.unacceptInvite(table);
        }
    }

    public enum InviteStatus {
        ACCEPT,
        DECLINE,
        UNACCEPT,
    }
}
