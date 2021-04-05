package io.papermc.hangar.controllerold;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.securityold.annotations.ProjectPermission;
import io.papermc.hangar.securityold.annotations.UserLock;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.project.ProjectFactory;
import io.papermc.hangar.serviceold.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

@Controller("oldProjectsController")
@Deprecated(forRemoval = true)
public class ProjectsController extends HangarController {

    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final UserActionLogService userActionLogService;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;

    @Autowired
    public ProjectsController(ProjectService projectService, ProjectFactory projectFactory, UserActionLogService userActionLogService, HttpServletRequest request, Supplier<ProjectsTable> projectsTable) {
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.userActionLogService = userActionLogService;
        this.request = request;
        this.projectsTable = projectsTable;
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

