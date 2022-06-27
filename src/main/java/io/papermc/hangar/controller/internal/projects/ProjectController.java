package io.papermc.hangar.controller.internal.projects;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.InternalHangarException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.api.responses.PossibleProjectOwner;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.uploads.ImageService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.users.invites.ProjectInviteService;

@Controller
@RateLimit(path = "project")
@RequestMapping(path = "/api/internal/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController extends HangarComponent {

    private final ProjectFactory projectFactory;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final ProjectInviteService projectInviteService;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final ImageService imageService;
    private final StatService statService;

    @Autowired
    public ProjectController(ProjectFactory projectFactory, ProjectService projectService, UserService userService, OrganizationService organizationService, ProjectMemberService projectMemberService, ProjectInviteService projectInviteService, ImageService imageService, StatService statService) {
        this.projectFactory = projectFactory;
        this.projectService = projectService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.projectMemberService = projectMemberService;
        this.projectInviteService = projectInviteService;
        this.imageService = imageService;
        this.statService = statService;
    }

    @LoggedIn
    @GetMapping("/validateName")
    @ResponseStatus(HttpStatus.OK)
    public void validateProjectName(@RequestParam long userId, @RequestParam String value) {
        projectFactory.checkProjectAvailability(userId, value);
    }

    @LoggedIn
    @GetMapping("/possibleOwners")
    public ResponseEntity<List<PossibleProjectOwner>> possibleProjectCreators() {
        List<PossibleProjectOwner> possibleProjectOwners = organizationService.getOrganizationTablesWithPermission(getHangarPrincipal().getId(), Permission.CreateProject).stream().map(PossibleProjectOwner::new).collect(Collectors.toList());
        possibleProjectOwners.add(0, new PossibleProjectOwner(getHangarPrincipal()));
        return ResponseEntity.ok(possibleProjectOwners);
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createProject(@RequestBody @Valid NewProjectForm newProject) {
        ProjectTable projectTable = projectFactory.createProject(newProject);
        // need to do this here, outside of the transactional
        projectService.refreshHomeProjects();
        return ResponseEntity.ok(projectTable.getUrl());
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @GetMapping("/project/{author}/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<HangarProject> getHangarProject(@PathVariable String author, @PathVariable String slug) {
        HangarProject hangarProject = projectService.getHangarProject(author, slug);
        statService.addProjectView(hangarProject);
        return ResponseEntity.ok(hangarProject);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody ProjectSettingsForm settingsForm) {
        projectService.saveSettings(author, slug, settingsForm);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/sponsors", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable String author, @PathVariable String slug, @RequestBody @Valid StringContent content) {
        if (content.getContent().length() > config.projects.getMaxSponsorsLen()) {
            throw new HangarApiException("page.new.error.name.maxLength");
        }
        projectService.saveSponsors(author, slug, content);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/saveIcon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveProjectIcon(@PathVariable String author, @PathVariable String slug, @RequestParam MultipartFile projectIcon) {
        projectService.saveIcon(author, slug, projectIcon);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping("/project/{author}/{slug}/resetIcon")
    public void resetProjectIcon(@PathVariable String author, @PathVariable String slug) {
        projectService.resetIcon(author, slug);
    }

    @Unlocked
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/project/{author}/{slug}/rename", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renameProject(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody StringContent nameContent) {
        return ResponseEntity.ok(projectFactory.renameProject(author, slug, nameContent.getContent()));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/members/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addProjectMember(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody EditMembersForm.Member<ProjectRole> member) {
        ProjectTable projectTable = projectService.getProjectTable(author, slug);
        projectInviteService.sendInvite(member, projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/members/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProjectMember(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody EditMembersForm.Member<ProjectRole> member) {
        ProjectTable projectTable = projectService.getProjectTable(author, slug);
        projectMemberService.editMember(member, projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/members/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeProjectMember(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody EditMembersForm.Member<ProjectRole> member) {
        ProjectTable projectTable = projectService.getProjectTable(author, slug);
        projectMemberService.removeMember(member, projectTable);
    }

    @Unlocked
    @VisibilityRequired(type = Type.PROJECT, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping("/project/{id}/star/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectStarred(@PathVariable("id") long projectId, @PathVariable boolean state) {
        userService.toggleStarred(projectId, state);
    }

    @Unlocked
    @VisibilityRequired(type = Type.PROJECT, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping("/project/{id}/watch/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectWatching(@PathVariable("id") long projectId, @PathVariable boolean state) {
        userService.toggleWatching(projectId, state);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.DELETE_PROJECT, args = "{#project}")
    @PostMapping(path = "/project/{projectId}/manage/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void softDeleteProject(@PathVariable("projectId") ProjectTable project, @RequestBody @Valid StringContent commentContent) {
        projectFactory.softDelete(project, commentContent.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermissionRequired(NamedPermission.HARD_DELETE_PROJECT)
    @PostMapping(path = "/project/{projectId}/manage/hardDelete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void hardDeleteProject(@PathVariable("projectId") ProjectTable project, @RequestBody @Valid StringContent commentContent) {
        projectFactory.hardDelete(project, commentContent.getContent());
    }

    // Can't put visibility required because the browser image requests don't include the JWT needed for authorization
    @Anyone
    @GetMapping(path = "/project/{author}/{slug}/icon", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public Object getProjectIcon(@PathVariable String author, @PathVariable String slug) {
        try {
            return imageService.getProjectIcon(author, slug);
        } catch (InternalHangarException e) {
            return new RedirectView(imageService.getUserIcon(author));
        }
    }
}
