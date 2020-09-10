package io.papermc.hangar.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.OrganizationContext;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.UserOrganizationRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.security.annotations.OrganizationPermission;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.OrgFactory;
import io.papermc.hangar.service.OrgService;
import io.papermc.hangar.service.RoleService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrgController extends HangarController {

    private static final String STATUS_DECLINE = "decline";
    private static final String STATUS_ACCEPT = "accept";
    private static final String STATUS_UNACCEPT = "unaccept";

    private final AuthenticationService authenticationService;
    private final OrgService orgService;
    private final OrgFactory orgFactory;
    private final UserService userService;
    private final RoleService roleService;
    private final UserActionLogService userActionLogService;
    private final NotificationService notificationService;
    private final HangarConfig hangarConfig;

    private final HttpServletRequest request;

    @Autowired
    public OrgController(AuthenticationService authenticationService, OrgService orgService, OrgFactory orgFactory, UserService userService, RoleService roleService, UserActionLogService userActionLogService, NotificationService notificationService, HangarConfig hangarConfig, HttpServletRequest request) {
        this.authenticationService = authenticationService;
        this.orgService = orgService;
        this.orgFactory = orgFactory;
        this.userService = userService;
        this.roleService = roleService;
        this.userActionLogService = userActionLogService;
        this.notificationService = notificationService;
        this.hangarConfig = hangarConfig;
        this.request = request;
    }

    @Secured("ROLE_USER")
    @PostMapping("/organizations/invite/{id}/{status}")
    @ResponseStatus(HttpStatus.OK)
    public void setInviteStatus(@PathVariable long id, @PathVariable String status) {
        UserOrganizationRolesTable orgRole = roleService.getUserOrgRole(id);
        if (orgRole == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        switch (status) {
            case STATUS_DECLINE:
                roleService.removeRole(orgRole.getOrganizationId(), orgRole.getUserId());
                break;
            case STATUS_ACCEPT:
                orgRole.setIsAccepted(true);
                roleService.updateRole(orgRole);
                break;
            case STATUS_UNACCEPT:
                orgRole.setIsAccepted(false);
                roleService.updateRole(orgRole);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @UserLock
    @Secured("ROLE_USER")
    @GetMapping("/organizations/new")
    public ModelAndView showCreator(RedirectAttributes attributes, ModelMap modelMap) {
        if (orgService.getUserOwnedOrgs(getCurrentUser().getId()).size() >= hangarConfig.org.getCreateLimit()) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.org.createLimit", String.valueOf(hangarConfig.org.getCreateLimit()));
            return Routes.SHOW_HOME.getRedirect();
        }
        return fillModel(AlertUtil.transferAlerts(new ModelAndView("createOrganization"), modelMap));
    }

    @UserLock
    @Secured("ROLE_USER")
    @PostMapping(value = "/organizations/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView create(@RequestParam String name, @RequestParam(required = false) List<Long> users, @RequestParam(required = false) List<Role> roles, RedirectAttributes attributes) {
        UsersTable currUser = getCurrentUser();
        if (orgService.getUserOwnedOrgs(getCurrentUser().getId()).size() >= hangarConfig.org.getCreateLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at create limit");
        }
        if (currUser.isLocked()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (!hangarConfig.org.isEnabled()) {
            AlertUtil.showAlert(attributes, AlertType.ERROR, "error.org.disabled");
            return Routes.ORG_SHOW_CREATOR.getRedirect();
        } else {
            Map<Long, Role> userRoles = zip(users, roles);

            OrganizationsTable org;
            try {
                org = orgFactory.createOrganization(name, currUser.getId(), userRoles);
            } catch (HangarException e) {
                AlertUtil.showAlert(attributes, AlertType.ERROR, e.getMessageKey(), e.getArgs());
                return Routes.ORG_SHOW_CREATOR.getRedirect();
            }
            return Routes.USERS_SHOW_PROJECTS.getRedirect(org.getName());
        }
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
    @PostMapping("/organizations/{organization}/settings/members")
    public ModelAndView updateMembers(@PathVariable String organization,
                                      @RequestParam(required = false) List<Long> users,
                                      @RequestParam(required = false) List<Role> roles,
                                      @RequestParam(required = false) List<String> userUps,
                                      @RequestParam(required = false) List<Role> roleUps) {
        OrganizationsTable org = orgService.getOrganization(organization); // Won't be null because the PreAuth check should catch that
        Map<Long, Role> userRoles = zip(users, roles);
        List<String> newState = new ArrayList<>();
        userRoles.forEach((memberId, role) -> {
            UsersTable memberUser = userService.getUsersTable(memberId);
            newState.add(memberUser.getName() + ": " + role.getTitle());
            roleService.addOrgMemberRole(org.getId(), memberId, role, false);
            notificationService.sendNotification(memberId, org.getId(), NotificationType.ORGANIZATION_INVITE, new String[]{"notification.organization.invite", role.getTitle(), org.getName()});
        });
        if (!newState.isEmpty()) {
            userActionLogService.organization(request, LoggedActionType.ORG_MEMBERS_ADDED.with(OrganizationContext.of(org.getId())), String.join("<br>", newState), "");
            newState.clear();
        }

        Map<UsersTable, Role> userRoleUpdates = zip(userService.getUsers(userUps), roleUps);
        userRoleUpdates.forEach((user, role) -> {
            newState.add(user.getName() + ": " + role.getTitle());
            roleService.updateRole(org, user.getId(), role);
        });
        if (!newState.isEmpty()) {
            userActionLogService.organization(request, LoggedActionType.ORG_MEMBER_ROLES_UPDATED.with(OrganizationContext.of(org.getId())), String.join("<br>", newState), "");
        }
        return Routes.USERS_SHOW_PROJECTS.getRedirect(organization);
    }

    @OrganizationPermission(NamedPermission.MANAGE_SUBJECT_MEMBERS)
    @Secured("ROLE_USER")
    @PostMapping("/organizations/{organization}/settings/members/remove")
    public ModelAndView removeMember(@PathVariable String organization, @RequestParam String username) {
        OrganizationsTable org = orgService.getOrganization(organization);
        UserData user = userService.getUserData(username);
        if (roleService.removeMember(org, user.getUser().getId()) != 0) {
            userActionLogService.organization(request, LoggedActionType.ORG_MEMBER_REMOVED.with(OrganizationContext.of(org.getId())), "Removed " + user.getUser().getName(), "");
        }
        return Routes.USERS_SHOW_PROJECTS.getRedirect(organization);
    }

    private <K, V> Map<K, V> zip(List<K> keys, List<V> values) {
        Map<K, V> map = new HashMap<>();
        if (keys != null && values != null) {
            if (keys.size() != values.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }
        }
        return map;

    }

}

