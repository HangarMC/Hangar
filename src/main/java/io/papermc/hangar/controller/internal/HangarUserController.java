package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.UserContext;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.roles.MemberService;
import io.papermc.hangar.service.internal.roles.MemberService.OrganizationMemberService;
import io.papermc.hangar.service.internal.roles.MemberService.ProjectMemberService;
import io.papermc.hangar.service.internal.roles.RoleService;
import io.papermc.hangar.service.internal.roles.RoleService.OrganizationRoleService;
import io.papermc.hangar.service.internal.roles.RoleService.ProjectRoleService;
import io.papermc.hangar.service.internal.users.InviteService;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET, RequestMethod.POST })
public class HangarUserController extends HangarController {

    private final ObjectMapper mapper;
    private final UsersApiService usersApiService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final InviteService inviteService;
    private final ProjectRoleService projectRoleService;
    private final OrganizationRoleService organizationRoleService;
    private final MemberService.ProjectMemberService projectMemberService;
    private final MemberService.OrganizationMemberService organizationMemberService;

    @Autowired
    public HangarUserController(ObjectMapper mapper, UsersApiService usersApiService, UserService userService, NotificationService notificationService, InviteService inviteService, ProjectRoleService projectRoleService, OrganizationRoleService organizationRoleService, ProjectMemberService projectMemberService, OrganizationMemberService organizationMemberService) {
        this.mapper = mapper;
        this.usersApiService = usersApiService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.inviteService = inviteService;
        this.projectRoleService = projectRoleService;
        this.organizationRoleService = organizationRoleService;
        this.projectMemberService = projectMemberService;
        this.organizationMemberService = organizationMemberService;
    }

    @GetMapping("/users/@me")
    public ResponseEntity<HangarUser> getCurrentUser(HangarAuthenticationToken hangarAuthenticationToken) {
        return ResponseEntity.ok(usersApiService.getUser(hangarAuthenticationToken.getName(), HangarUser.class));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(perms = NamedPermission.EDIT_OWN_USER_SETTINGS)
    @PostMapping(path = "/users/{userName}/settings/tagline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveTagline(@PathVariable String userName, @Valid @RequestBody StringContent content) {
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        // TODO what perm to check here
        if (userTable.getId() != getHangarPrincipal().getId() && !getGlobalPermissions().has(Permission.ManualValueChanges)) {
            throw new HangarApiException(HttpStatus.FORBIDDEN);
        }
        if (content.getContent().length() > config.user.getMaxTaglineLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "author.error.invalidTagline");
        }
        String oldTagline = userTable.getTagline() == null ? "" : userTable.getTagline();
        userTable.setTagline(content.getContent());
        userService.updateUser(userTable);
        userActionLogService.user(LoggedActionType.USER_TAGLINE_CHANGED.with(UserContext.of(userTable.getId())), userTable.getTagline(), oldTagline);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(perms = NamedPermission.EDIT_OWN_USER_SETTINGS)
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
        userActionLogService.user(LoggedActionType.USER_TAGLINE_CHANGED.with(UserContext.of(userTable.getId())), "", oldTagline);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<HangarNotification>> getUserNotifications() {
        return ResponseEntity.ok(notificationService.getUsersNotifications());
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
        invites.set("projects", mapper.valueToTree(inviteService.getProjectInvites()));
        invites.set("organizations", mapper.valueToTree(inviteService.getOrganizationInvites()));
        return ResponseEntity.ok(invites);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/invites/projects/{id}/{status}")
    public void updateProjectInviteStatus(@PathVariable long id, @PathVariable InviteStatus status) {
        updateRole(projectRoleService, projectMemberService, id, status);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/invites/organizations/{id}/{status}")
    public void updateOrganizationInviteStatus(@PathVariable long id, @PathVariable InviteStatus status) {
        updateRole(organizationRoleService, organizationMemberService, id, status);
    }

    private <RT extends ExtendedRoleTable<? extends Role<RT>>, RS extends RoleService<RT, ?, ?>, MS extends MemberService<?, RT, ?, RS, ?, ?>> void updateRole(RS roleService, MS memberService,  long id, InviteStatus status) {
        RT table = roleService.getRole(id);
        if (table == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        switch (status) {
            case DECLINE:
                memberService.removeMember(table);
                break;
            case ACCEPT:
            case UNACCEPT:
                table.setAccepted(status == InviteStatus.ACCEPT);
                roleService.updateRole(table);
                break;
        }
    }

    public enum InviteStatus {
        ACCEPT,
        DECLINE,
        UNACCEPT,
    }

}
