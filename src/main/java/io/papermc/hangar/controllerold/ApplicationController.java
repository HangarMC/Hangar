package io.papermc.hangar.controllerold;

import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import io.papermc.hangar.controllerold.forms.UserAdminForm;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.modelold.RoleTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.modelold.Role;
import io.papermc.hangar.modelold.viewhelpers.Activity;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.serviceold.OrgService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.UserService;
import io.papermc.hangar.serviceold.project.ProjectService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Controller("oldApplicationController")
@Deprecated(forRemoval = true)
public class ApplicationController extends HangarController {

    private final UserService userService;
    private final ProjectService projectService;
    private final OrgService orgService;
    private final UserActionLogService userActionLogService;
    private final RoleService roleService;

    private final Supplier<UserData> userData;

    @Autowired
    public ApplicationController(UserService userService, ProjectService projectService, OrgService orgService, UserActionLogService userActionLogService, RoleService roleService, Supplier<UserData> userData) {
        this.userService = userService;
        this.projectService = projectService;
        this.orgService = orgService;
        this.userActionLogService = userActionLogService;
        this.roleService = roleService;
        this.userData = userData;
    }



    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @GetMapping("/admin/activities/{user}")
    public ModelAndView showActivities(@PathVariable String user) {
        ModelAndView mv = new ModelAndView("users/admin/activity");
        mv.addObject("username", user);
        List<Activity> activities = new ArrayList<>();
        activities.addAll(userService.getFlagActivity(user));
        activities.addAll(userService.getReviewActivity(user));
        mv.addObject("activities", activities);
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.VIEW_LOGS)
    @Secured("ROLE_USER")
    @GetMapping("/admin/log")
    public ModelAndView showLog(@RequestParam(required = false) Integer oPage,
                                @RequestParam(required = false) String userFilter,
                                @RequestParam(required = false) String projectFilter,
                                @RequestParam(required = false) String versionFilter,
                                @RequestParam(required = false) String pageFilter,
                                @RequestParam(required = false) String actionFilter,
                                @RequestParam(required = false) String subjectFilter) {
        ModelAndView mv = new ModelAndView("users/admin/log");
        int pageSize = 50;
        int page = oPage != null ? oPage : 1;
        int offset = (page - 1) * pageSize;
        List<HangarLoggedAction> log = userActionLogService.getLog(oPage, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter);
        mv.addObject("actions", log);
        mv.addObject("limit", pageSize);
        mv.addObject("offset", offset);
        mv.addObject("page", page);
        mv.addObject("size", 10); //TODO sum of table sizes of all LoggedAction tables (I think)
        mv.addObject("userFilter", userFilter);
        //TODO filter slug and author, not just slug (temp fix was to just concatenate the owner and slug together)
        mv.addObject("projectFilter", projectFilter);
        mv.addObject("versionFilter", versionFilter);
        mv.addObject("pageFilter", pageFilter);
        mv.addObject("actionFilter", actionFilter);
        mv.addObject("subjectFilter", subjectFilter);
        mv.addObject("canViewIP", userService.getHeaderData().getGlobalPermission().has(Permission.ViewIp));
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/admin/user/{user}")
    public ModelAndView userAdmin(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/admin/userAdmin");
        UserData userData = userService.getUserData(user);
        mav.addObject("u", userData);
        OrganizationData organizationData = orgService.getOrganizationData(userData.getUser());
        mav.addObject("orga", organizationData);
        mav.addObject("userProjectRoles", projectService.getProjectsAndRoles(userData.getUser().getId()));
        return fillModel(mav);
    }


    private static final String ORG_ROLE = "orgRole";
    private static final String MEMBER_ROLE = "memberRole";
    private static final String PROJECT_ROLE = "projectRole";
    @GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @Secured("ROLE_USER")
    @PostMapping(value = "/admin/user/{user}/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateUser(@PathVariable("user") String userName, @RequestBody UserAdminForm userAdminForm) {
        UserData user = userData.get();
        switch (userAdminForm.getThing()) {
            case ORG_ROLE: {
                if (user.isOrga()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                UserOrganizationRolesTable orgRole = roleService.getUserOrgRole(userAdminForm.getData().get("id").asLong());
                break;
            }
            case MEMBER_ROLE: {
                if (!user.isOrga()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                UserOrganizationRolesTable orgRole = roleService.getUserOrgRole(userAdminForm.getData().get("id").asLong());
                updateRoleTable(userAdminForm, orgRole, RoleCategory.ORGANIZATION, Role.ORGANIZATION_OWNER);
                break;
            }
            case PROJECT_ROLE: {
                UserProjectRolesTable projectRole = roleService.getUserProjectRole(userAdminForm.getData().get("id").asLong());
                updateRoleTable(userAdminForm, projectRole, RoleCategory.PROJECT, Role.PROJECT_OWNER);
                break;
            }
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static final String SET_ROLE = "setRole";
    private static final String SET_ACCEPTED = "setAccepted";
    private static final String DELETE_ROLE = "deleteRole";
    private <R extends RoleTable> void updateRoleTable(UserAdminForm userAdminForm, R userRole, RoleCategory allowedCategory, Role ownerType) {
        switch (userAdminForm.getAction()) {
            case SET_ROLE:
                Role role = Role.fromId(userAdminForm.getData().get("role").asLong());
                if (role == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                else if (role == ownerType) {
                    // TODO transfer owner
                }
                else if (role.getCategory() == allowedCategory && role.isAssignable()) {
                    userRole.setRoleType(role.getValue());
                    roleService.updateRole(userRole);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                break;
            case SET_ACCEPTED:
                userRole.setAccepted(true);
                roleService.updateRole(userRole);
                break;
            case DELETE_ROLE:
                if (userRole.getRole().isAssignable()) {
                    roleService.removeRole(userRole);
                }
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/assets-ext/css/admonition.css", produces = "text/css")
    @ResponseBody
    public String admonitionCss() {
        return AdmonitionExtension.getDefaultCSS();
    }

    @GetMapping(value = "/assets-ext/js/admonition.js", produces = "text/javascript")
    @ResponseBody
    public String admonitionJs() {
        return AdmonitionExtension.getDefaultScript().replace("(() => {", "window.admonition = () => {").replace("})();", "};");
    }
}
