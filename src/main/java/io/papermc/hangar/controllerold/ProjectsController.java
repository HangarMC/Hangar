package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.generated.Note;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ScopedProjectData;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.securityold.annotations.ProjectPermission;
import io.papermc.hangar.securityold.annotations.UserLock;
import io.papermc.hangar.serviceold.OrgService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.StatsService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.UserService;
import io.papermc.hangar.serviceold.project.FlagService;
import io.papermc.hangar.serviceold.project.ProjectFactory;
import io.papermc.hangar.serviceold.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Controller("oldProjectsController")
@Deprecated(forRemoval = true)
public class ProjectsController extends HangarController {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");
    private static final String STATUS_DECLINE = "decline";
    private static final String STATUS_ACCEPT = "accept";
    private static final String STATUS_UNACCEPT = "unaccept";

    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final OrgService orgService;
    private final FlagService flagService;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final RoleService roleService;
    private final UserActionLogService userActionLogService;
    private final StatsService statsService;
    private final HangarDao<ProjectDao> projectDao;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;

    @Autowired
    public ProjectsController(HangarConfig hangarConfig, UserService userService, OrgService orgService, FlagService flagService, ProjectService projectService, ProjectFactory projectFactory, RoleService roleService, UserActionLogService userActionLogService, StatsService statsService, HangarDao<ProjectDao> projectDao, HttpServletRequest request, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.orgService = orgService;
        this.flagService = flagService;
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.roleService = roleService;
        this.userActionLogService = userActionLogService;
        this.statsService = statsService;
        this.projectDao = projectDao;
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

    @GetMapping("/{author}/{slug}/discuss")
    public ModelAndView showDiscussion(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/discuss");
        ProjectData projData = projectData.get();
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projData.getProject().getId());
        mv.addObject("p", projData);
        mv.addObject("sp", scopedProjectData);
        statsService.addProjectView(projData.getProject()); // TODO this is in ore, but I'm not sure why
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/discuss/reply")
    public Object postDiscussionReply(@PathVariable String author, @PathVariable String slug) {
        return null; // TODO implement postDiscussionReply request controller
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/flag", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView flag(@PathVariable String author, @PathVariable String slug, @RequestParam("flag-reason") FlagReason flagReason, @RequestParam String comment) {
        ProjectsTable project = projectsTable.get();
        if (flagService.hasUnresolvedFlag(project.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 flag at a time per project per user");
        } else if (comment.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must not be blank");
        }
        flagService.flagProject(project.getId(), flagReason, comment);
        String userName = getCurrentUser().getName();
        userActionLogService.project(request, LoggedActionType.PROJECT_FLAGGED.with(ProjectContext.of(project.getId())), "Flagged by " + userName, "Not flagged by " + userName);
        return Routes.PROJECTS_SHOW.getRedirect(author, slug); // TODO flashing
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/flags")
    public ModelAndView showFlags(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/admin/flags");
        mav.addObject("p", projectData.get());
        return fillModel(mav);
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

    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/manage/sendforapproval")
    public ModelAndView sendForApproval(@PathVariable String author, @PathVariable String slug) {
        ProjectsTable project = projectsTable.get();
        if (project.getVisibility() == Visibility.NEEDSCHANGES) {
            projectService.changeVisibility(project, Visibility.NEEDSAPPROVAL, "");
            userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), Visibility.NEEDSAPPROVAL.getName(), Visibility.NEEDSCHANGES.getName());
        }
        return Routes.PROJECTS_SHOW.getRedirect(author, slug);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/notes")
    public ModelAndView showNotes(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/admin/notes");
        ProjectsTable project = projectsTable.get();

        List<Note> notes = new ArrayList<>();
        ArrayNode messages = (ArrayNode) project.getNotes().getJson().get("messages");
        if (messages != null) {
            for (JsonNode message : messages) {
                Note note = new Note().message(message.get("message").asText());
                notes.add(note);
                UserData user = userService.getUserData(message.get("user").asLong());
                note.user(user.getUser().getName());
            }
        }

        mv.addObject("project", project);
        mv.addObject("notes", notes);
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/notes/addmessage")
    public ResponseEntity<String> addMessage(@PathVariable String author, @PathVariable String slug, @RequestParam String content) {
        ProjectsTable project = projectsTable.get();
        ArrayNode messages = project.getNotes().getJson().withArray("messages");
        ObjectNode note = messages.addObject();
        note.put("message", content);
        note.put("user", getCurrentUser().getId());

        String json = project.getNotes().getJson().toString();
        projectDao.get().updateNotes(json, project.getId());
        return ResponseEntity.ok("Review");
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/visible/{visibility}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void setVisible(@PathVariable String author,
                           @PathVariable String slug,
                           @PathVariable Visibility visibility,
                           @RequestParam(required = false) String comment) {
        ProjectsTable project = projectsTable.get();
        Visibility oldVisibility = project.getVisibility();
        projectService.changeVisibility(project, visibility, comment);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), visibility.getName(), oldVisibility.getName());
        projectService.refreshHomePage();
    }
}

