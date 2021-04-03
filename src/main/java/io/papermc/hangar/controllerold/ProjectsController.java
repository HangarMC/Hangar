package io.papermc.hangar.controllerold;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.securityold.annotations.ProjectPermission;
import io.papermc.hangar.securityold.annotations.UserLock;
import io.papermc.hangar.serviceold.OrgService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.StatsService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.project.ProjectFactory;
import io.papermc.hangar.serviceold.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Controller("oldProjectsController")
@Deprecated(forRemoval = true)
public class ProjectsController extends HangarController {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");
    private static final String STATUS_DECLINE = "decline";
    private static final String STATUS_ACCEPT = "accept";
    private static final String STATUS_UNACCEPT = "unaccept";

    private final OrgService orgService;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final RoleService roleService;
    private final UserActionLogService userActionLogService;
    private final StatsService statsService;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;

    @Autowired
    public ProjectsController(OrgService orgService, ProjectService projectService, ProjectFactory projectFactory, RoleService roleService, UserActionLogService userActionLogService, StatsService statsService, HttpServletRequest request, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.orgService = orgService;
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.roleService = roleService;
        this.userActionLogService = userActionLogService;
        this.statsService = statsService;
        this.request = request;
        this.projectsTable = projectsTable;
        this.projectData = projectData;
    }

    @Secured("ROLE_USER")
    @PostMapping("/invite/{id}/{status}/{behalf}")
    @ResponseStatus(HttpStatus.OK)
    public void setInviteStatusOnBehalf(@PathVariable long id, @PathVariable String status, @PathVariable String behalf) {
        UserProjectRolesTable projectRole = roleService.getUserProjectRole(id);
        OrganizationsTable organizationsTable = orgService.getOrganization(behalf);
        if (projectRole == null || organizationsTable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ScopedOrganizationData scopedOrganizationData = orgService.getScopedOrganizationData(organizationsTable);
        if (!scopedOrganizationData.getPermissions().has(Permission.ManageProjectMembers)) {
            if (getCurrentUser() != null) { // getCurrentUser() handles throwing UNAUTHORIZED
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        updateRole(status, projectRole);
    }

    private void updateRole(String status, UserProjectRolesTable projectRole) {
        switch (status) {
            case STATUS_DECLINE:
                roleService.removeRole(projectRole);
                break;
            case STATUS_ACCEPT:
                projectRole.setAccepted(true);
                roleService.updateRole(projectRole);
                break;
            case STATUS_UNACCEPT:
                projectRole.setAccepted(false);
                roleService.updateRole(projectRole);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @ProjectPermission(NamedPermission.DELETE_PROJECT)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView softDelete(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String comment, RedirectAttributes ra) {
        ProjectsTable project = projectsTable.get();
        Visibility oldVisibility = project.getVisibility();

        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), Visibility.SOFTDELETE.getName(), oldVisibility.getName());
        projectFactory.softDeleteProject(project, comment);
        AlertUtil.showAlert(ra, AlertUtil.AlertType.SUCCESS, "project.deleted", project.getName());
        projectService.refreshHomePage();
        return Routes.SHOW_HOME.getRedirect();
    }

    @GlobalPermission(NamedPermission.HARD_DELETE_PROJECT)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/manage/hardDelete")
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, RedirectAttributes ra) {
        ProjectsTable project = projectsTable.get();
        projectFactory.hardDeleteProject(project);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), "deleted", project.getVisibility().getName());
        AlertUtil.showAlert(ra, AlertUtil.AlertType.SUCCESS, "project.deleted", project.getName());
        projectService.refreshHomePage();
        return Routes.SHOW_HOME.getRedirect();
    }

}

