package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.model.roles.Role;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.InviteService;
import io.papermc.hangar.service.internal.MemberService;
import io.papermc.hangar.service.internal.NotificationService;
import io.papermc.hangar.service.internal.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.roles.ProjectRoleService;
import io.papermc.hangar.service.internal.roles.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET, RequestMethod.POST })
public class HangarUsersController extends HangarController {

    private final ObjectMapper mapper;
    private final UsersApiService usersApiService;
    private final NotificationService notificationService;
    private final InviteService inviteService;
    private final ProjectRoleService projectRoleService;
    private final OrganizationRoleService organizationRoleService;
    private final MemberService.ProjectMemberService projectMemberService;
    private final MemberService.OrganizationMemberService organizationMemberService;

    @Autowired
    public HangarUsersController(ObjectMapper mapper, UsersApiService usersApiService, NotificationService notificationService, InviteService inviteService, ProjectRoleService projectRoleService, OrganizationRoleService organizationRoleService, MemberService.ProjectMemberService projectMemberService, MemberService.OrganizationMemberService organizationMemberService) {
        this.mapper = mapper;
        this.usersApiService = usersApiService;
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

    @GetMapping("/notifications")
    public ResponseEntity<List<HangarNotification>> getUserNotifications() {
        return ResponseEntity.ok(notificationService.getUsersNotifications());
    }

    @PostMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markNotificationAsRead(@PathVariable long id) {
        if (!notificationService.markNotificationAsRead(id)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No notification found");
        }
    }

    @GetMapping("/invites")
    public ResponseEntity<ObjectNode> getUserInvites() {
        ObjectNode invites = mapper.createObjectNode();
        invites.set("projects", mapper.valueToTree(inviteService.getProjectInvites()));
        invites.set("organizations", mapper.valueToTree(inviteService.getOrganizationInvites()));
        return ResponseEntity.ok(invites);
    }

    @PostMapping("/invites/projects/{id}/{status}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProjectInviteStatus(@PathVariable long id, @PathVariable InviteStatus status) {
        updateRole(projectRoleService, projectMemberService, id, status);
    }

    @PostMapping("/invites/organizations/{id}/{status}")
    public void updateOrganizationInviteStatus(@PathVariable long id, @PathVariable InviteStatus status) {
        updateRole(organizationRoleService, organizationMemberService, id, status);
    }

    private <RT extends ExtendedRoleTable<? extends Role<RT>>, RS extends RoleService<RT, ?, ?>, MS extends MemberService<?, RT, ?, RS, ?, ?>> void updateRole(RS roleService, MS memberService,  long id, InviteStatus status) {
        RT table = roleService.getRole(id);
        if (table == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Invalid role id");
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
