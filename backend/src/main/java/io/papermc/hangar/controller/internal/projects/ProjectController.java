package io.papermc.hangar.controller.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.api.responses.PossibleProjectOwner;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.components.stats.StatService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.perms.members.ProjectMemberService;
import io.papermc.hangar.service.internal.projects.PinnedProjectService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.users.invites.ProjectInviteService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// @el(author: String, slug: String, projectId: long, project: io.papermc.hangar.model.db.projects.ProjectTable)
@RestController
@RateLimit(path = "project")
@RequestMapping(path = "/api/internal/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController extends HangarComponent {

    private final ProjectFactory projectFactory;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final ProjectInviteService projectInviteService;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final StatService statService;
    private final PinnedProjectService pinnedProjectService;

    @Autowired
    public ProjectController(final ProjectFactory projectFactory, final ProjectService projectService, final UserService userService, final OrganizationService organizationService, final ProjectMemberService projectMemberService, final ProjectInviteService projectInviteService, final StatService statService, final PinnedProjectService pinnedProjectService) {
        this.projectFactory = projectFactory;
        this.projectService = projectService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.projectMemberService = projectMemberService;
        this.projectInviteService = projectInviteService;
        this.statService = statService;
        this.pinnedProjectService = pinnedProjectService;
    }

    @LoggedIn
    @GetMapping("/validateName")
    @ResponseStatus(HttpStatus.OK)
    public void validateProjectName(@RequestParam final String value) {
        this.projectFactory.checkProjectAvailability(value);
    }

    @LoggedIn
    @GetMapping("/possibleOwners")
    public ResponseEntity<List<PossibleProjectOwner>> possibleProjectCreators() {
        final List<PossibleProjectOwner> possibleProjectOwners = this.organizationService.getOrganizationTablesWithPermission(this.getHangarPrincipal().getId(), Permission.CreateProject).stream().map(PossibleProjectOwner::new).collect(Collectors.toList());
        possibleProjectOwners.addFirst(new PossibleProjectOwner(this.getHangarPrincipal()));
        return ResponseEntity.ok(possibleProjectOwners);
    }

    @Unlocked
    @RequireAal(1)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createProject(@RequestBody @Valid final NewProjectForm newProject) {
        final ProjectTable projectTable = this.projectFactory.createProject(newProject);
        return ResponseEntity.ok(projectTable.getUrl());
    }

    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    @GetMapping("/project/{slugOrId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<HangarProject> getHangarProject(@PathVariable("slugOrId") final ProjectTable project) {
        final HangarProject hangarProject = this.projectService.getHangarProject(project);
        this.statService.addProjectView(hangarProject);
        return ResponseEntity.ok(hangarProject);
    }

    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    @GetMapping("/project-redirect/{slugOrId}")
    public void projectRedirect(@PathVariable("slugOrId") final ProjectTable project) {
        final String url = this.projectService.getProjectUrlFromSlug(project);
        this.response.setStatus(301);
        this.response.setHeader("Location", url);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable("slugOrId") final ProjectTable project, @RequestBody final @Valid ProjectSettingsForm settingsForm) {
        this.projectService.saveSettings(project, settingsForm);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/sponsors", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable("slugOrId") final ProjectTable project, @RequestBody final StringContent content) {
        this.projectService.saveSponsors(project, content.getContent());
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/saveIcon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveProjectIcon(@PathVariable("slugOrId") final ProjectTable project, @RequestParam final MultipartFile projectIcon) throws IOException {
        this.projectService.changeAvatar(project, projectIcon.getBytes());
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#project}")
    @PostMapping("/project/{slugOrId}/resetIcon")
    public void resetProjectIcon(@PathVariable("slugOrId") final ProjectTable project) {
        this.projectService.deleteAvatar(project);
    }

    @Unlocked
    @RequireAal(1)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#project}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/project/{slugOrId}/rename", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renameProject(@PathVariable("slugOrId") final ProjectTable project, @RequestBody @Valid final StringContent nameContent) {
        return ResponseEntity.ok(this.projectFactory.renameProject(project, nameContent.getContent()));
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#project}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/project/{slugOrId}/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void transferProject(@PathVariable("slugOrId") final ProjectTable project, @RequestBody @Valid final StringContent nameContent) {
        this.projectInviteService.sendTransferRequest(nameContent.getContent(), project);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/canceltransfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelProjectTransfer(@PathVariable("slugOrId") final ProjectTable project) {
        this.projectInviteService.cancelTransferRequest(project);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/members/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addProjectMember(@PathVariable("slugOrId") final ProjectTable project, @RequestBody @Valid final EditMembersForm.ProjectMember member) {
        this.projectInviteService.sendInvite(member, project);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/members/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProjectMember(@PathVariable("slugOrId") final ProjectTable project, @RequestBody @Valid final EditMembersForm.ProjectMember member) {
        this.projectMemberService.editMember(member, project);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/members/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeProjectMember(@PathVariable("slugOrId") final ProjectTable project, @RequestBody @Valid final EditMembersForm.ProjectMember member) {
        this.projectMemberService.removeMember(member, project);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/members/leave", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void leaveProject(@PathVariable("slugOrId") final ProjectTable project) {
        this.projectMemberService.leave(project);
    }

    @Unlocked
    @RequireAal(1)
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping("/project/{id}/star/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectStarred(@PathVariable("id") final long projectId, @PathVariable final boolean state) {
        this.userService.toggleStarred(this.getHangarPrincipal().getUserId(), projectId, state);
    }

    @Unlocked
    @RequireAal(1)
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping("/project/{id}/watch/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectWatching(@PathVariable("id") final long projectId, @PathVariable final boolean state) {
        this.userService.toggleWatching(this.getHangarPrincipal().getUserId(), projectId, state);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 3, refillSeconds = 10)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    @PostMapping(path = "/project/{slugOrId}/pin/{state}")
    public void setPinnedStatus(@PathVariable("slugOrId") final ProjectTable project, @PathVariable final boolean state) {
        if (state) {
            this.pinnedProjectService.addPinnedProject(this.getHangarUserId(), project.getId());
        } else {
            this.pinnedProjectService.removePinnedProject(this.getHangarUserId(), project.getId());
        }
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 45)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.DELETE_PROJECT, args = "{#project}")
    @PostMapping(path = "/project/{projectId}/manage/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void softDeleteProject(@PathVariable("projectId") final ProjectTable project, @RequestBody @Valid final StringContent commentContent) {
        this.projectFactory.softDelete(project, commentContent.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermissionRequired(NamedPermission.HARD_DELETE_PROJECT)
    @PostMapping(path = "/project/{projectId}/manage/hardDelete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void hardDeleteProject(@PathVariable("projectId") final ProjectTable project, @RequestBody @Valid final StringContent commentContent) {
        this.projectFactory.hardDelete(project, commentContent.getContent());
    }
}
