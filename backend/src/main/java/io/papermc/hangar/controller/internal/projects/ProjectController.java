package io.papermc.hangar.controller.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
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
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.internal.admin.StatService;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

// @el(author: String, slug: String, projectId: long, project: io.papermc.hangar.model.db.projects.ProjectTable)
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
        possibleProjectOwners.add(0, new PossibleProjectOwner(this.getHangarPrincipal()));
        return ResponseEntity.ok(possibleProjectOwners);
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createProject(@RequestBody @Valid final NewProjectForm newProject) {
        final ProjectTable projectTable = this.projectFactory.createProject(newProject);
        // need to do this here, outside the transactional
        this.projectService.refreshHomeProjects();
        return ResponseEntity.ok(projectTable.getUrl());
    }

    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#slug}")
    @GetMapping("/project/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<HangarProject> getHangarProject(@PathVariable final String slug) {
        final HangarProject hangarProject = this.projectService.getHangarProject(slug);
        this.statService.addProjectView(hangarProject);
        return ResponseEntity.ok(hangarProject);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable final String slug, @RequestBody final @Valid ProjectSettingsForm settingsForm) {
        this.projectService.saveSettings(slug, settingsForm);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/sponsors", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable final String slug, @RequestBody final StringContent content) {
        this.projectService.saveSponsors(slug, content.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/saveIcon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveProjectIcon(@PathVariable final String slug, @RequestParam final MultipartFile projectIcon) throws IOException {
        this.projectService.changeAvatar(slug, projectIcon.getBytes());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#slug}")
    @PostMapping("/project/{slug}/resetIcon")
    public void resetProjectIcon(@PathVariable final String slug) {
        this.projectService.deleteAvatar(slug);
    }

    @Unlocked
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#slug}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/project/{slug}/rename", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renameProject(@PathVariable final String slug, @RequestBody @Valid final StringContent nameContent) {
        return ResponseEntity.ok(this.projectFactory.renameProject(slug, nameContent.getContent()));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#slug}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @PostMapping(path = "/project/{slug}/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void transferProject(@PathVariable final String slug, @RequestBody @Valid final StringContent nameContent) {
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.projectInviteService.sendTransferRequest(nameContent.getContent(), projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_OWNER, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/canceltransfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelProjectTransfer(@PathVariable final String slug) {
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.projectInviteService.cancelTransferRequest(projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 2, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/members/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addProjectMember(@PathVariable final String slug, @RequestBody @Valid final EditMembersForm.Member<ProjectRole> member) {
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.projectInviteService.sendInvite(member, projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 7, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/members/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProjectMember(@PathVariable final String slug, @RequestBody @Valid final EditMembersForm.Member<ProjectRole> member) {
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.projectMemberService.editMember(member, projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.MANAGE_SUBJECT_MEMBERS, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/members/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeProjectMember(@PathVariable final String slug, @RequestBody @Valid final EditMembersForm.Member<ProjectRole> member) {
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.projectMemberService.removeMember(member, projectTable);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#slug}")
    @PostMapping(path = "/project/{slug}/members/leave", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void leaveProject(@PathVariable final String slug) {
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        this.projectMemberService.leave(projectTable);
    }

    @Unlocked
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping("/project/{id}/star/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectStarred(@PathVariable("id") final long projectId, @PathVariable final boolean state) {
        this.userService.toggleStarred(projectId, state);
    }

    @Unlocked
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping("/project/{id}/watch/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectWatching(@PathVariable("id") final long projectId, @PathVariable final boolean state) {
        this.userService.toggleWatching(projectId, state);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 10, refillTokens = 3, refillSeconds = 10)
    @PermissionRequired(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#projectId}")
    @PostMapping(path = "/project/{id}/pin/{state}")
    public void setPinnedStatus(@PathVariable("id") final long projectId, @PathVariable final boolean state) {
        if (state) {
            this.pinnedProjectService.addPinnedProject(this.getHangarUserId(), projectId);
        } else {
            this.pinnedProjectService.removePinnedProject(this.getHangarUserId(), projectId);
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
