package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.papermc.hangar.controllerold.forms.JoinableRoleUpdates;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.OrganizationContext;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.modelold.NotificationType;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.securityold.annotations.OrganizationPermission;
import io.papermc.hangar.serviceold.AuthenticationService;
import io.papermc.hangar.serviceold.NotificationService;
import io.papermc.hangar.serviceold.OrgService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.UserService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Controller("oldOrganizationController")
public class OrganizationController extends HangarController {

    private static final String STATUS_DECLINE = "decline";
    private static final String STATUS_ACCEPT = "accept";
    private static final String STATUS_UNACCEPT = "unaccept";

    private final AuthenticationService authenticationService;
    private final OrgService orgService;
    private final UserService userService;
    private final RoleService roleService;
    private final UserActionLogService userActionLogService;
    private final NotificationService notificationService;

    private final HttpServletRequest request;

    @Autowired
    public OrganizationController(AuthenticationService authenticationService, OrgService orgService, UserService userService, RoleService roleService, UserActionLogService userActionLogService, NotificationService notificationService, HttpServletRequest request) {
        this.authenticationService = authenticationService;
        this.orgService = orgService;
        this.userService = userService;
        this.roleService = roleService;
        this.userActionLogService = userActionLogService;
        this.notificationService = notificationService;
        this.request = request;
    }

    @OrganizationPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/organizations/{organization}/settings/avatar")
    public ModelAndView updateAvatar(@PathVariable String organization) {
        try {
            URI uri = authenticationService.changeAvatarUri(getCurrentUser().getName(), organization);
            return Routes.getRedirectToUrl(uri.toString());
        } catch (JsonProcessingException e) {
            ModelAndView mav = Routes.USERS_SHOW_PROJECTS.getRedirect(organization);
            AlertUtil.showAlert(mav, AlertType.ERROR, "organization.avatarFailed");
            return mav;
        }
    }

    @OrganizationPermission(NamedPermission.MANAGE_SUBJECT_MEMBERS)
    @Secured("ROLE_USER")
    @PostMapping(value = "/organizations/{organization}/settings/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateMembers(@PathVariable String organization, @RequestBody JoinableRoleUpdates orgRoleUpdates) {
        OrganizationsTable org = orgService.getOrganization(organization);

        List<String> newState = new ArrayList<>();
        orgRoleUpdates.getAdditions().forEach(userRole -> {
            if (userRole.getRole().getCategory() == RoleCategory.ORGANIZATION && userRole.getRole().isAssignable()) {
                UsersTable memberUser = userService.getUsersTable(userRole.getUserId());
                newState.add(memberUser.getName() + ": " + userRole.getRole().getTitle());
                roleService.addOrgMemberRole(org.getId(), memberUser.getId(), userRole.getRole(), false);
                notificationService.sendNotification(memberUser.getId(), org.getId(), NotificationType.ORGANIZATION_INVITE, new String[]{"notification.organization.invite", userRole.getRole().getTitle(), org.getName()});
            }
        });
        if (!newState.isEmpty()) {
            userActionLogService.organization(request, LoggedActionType.ORG_MEMBERS_ADDED.with(OrganizationContext.of(org.getId())), String.join("<br>", newState), "");
            newState.clear();
        }

        orgRoleUpdates.getUpdates().forEach(userRole -> {
            if (userRole.getRole().getCategory() == RoleCategory.ORGANIZATION && userRole.getRole().isAssignable()) {
                UsersTable memberUser = userService.getUsersTable(userRole.getUserId());
                newState.add(memberUser.getName() + ": " + userRole.getRole().getTitle());
                roleService.updateRole(org, memberUser.getId(), userRole.getRole());
            }
        });
        if (!newState.isEmpty()) {
            userActionLogService.organization(request, LoggedActionType.ORG_MEMBER_ROLES_UPDATED.with(OrganizationContext.of(org.getId())), String.join("<br>", newState), "");
        }
    }

    @OrganizationPermission(NamedPermission.MANAGE_SUBJECT_MEMBERS)
    @Secured("ROLE_USER")
    @PostMapping(value = "/organizations/{organization}/settings/members/remove", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView removeMember(@PathVariable String organization, @RequestParam String username) {
        OrganizationsTable org = orgService.getOrganization(organization);
        UserData user = userService.getUserData(username);
        if (roleService.removeMember(org, user.getUser().getId()) != 0) {
            userActionLogService.organization(request, LoggedActionType.ORG_MEMBER_REMOVED.with(OrganizationContext.of(org.getId())), "Removed " + user.getUser().getName(), "");
        }
        return Routes.USERS_SHOW_PROJECTS.getRedirect(organization);
    }

}

