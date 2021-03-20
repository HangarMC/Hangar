package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectForm;
import io.papermc.hangar.model.internal.api.requests.projects.ProjectSettingsForm;
import io.papermc.hangar.model.internal.api.responses.PossibleProjectOwner;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.internal.OrganizationService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController extends HangarController {

    private final ProjectFactory projectFactory;
    private final ProjectService projectService;
    private final UserService userService;
    private final OrganizationService organizationService;

    @Autowired
    public ProjectController(ProjectFactory projectFactory, ProjectService projectService, UserService userService, OrganizationService organizationService) {
        this.projectFactory = projectFactory;
        this.projectService = projectService;
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @GetMapping("/validateName")
    @ResponseStatus(HttpStatus.OK)
    public void validateProjectName(@RequestParam long userId, @RequestParam String value) {
        projectFactory.checkProjectAvailability(userId, value);
    }

    @GetMapping("/possibleOwners")
    public ResponseEntity<List<PossibleProjectOwner>> possibleProjectCreators() {
        List<PossibleProjectOwner> possibleProjectOwners = organizationService.getOrganizationTablesWithPermission(getHangarPrincipal().getId(), Permission.CreateProject).stream().map(PossibleProjectOwner::new).collect(Collectors.toList());
        possibleProjectOwners.add(new PossibleProjectOwner(getHangarPrincipal()));
        return ResponseEntity.ok(possibleProjectOwners);
    }

    @Unlocked
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createProject(@RequestBody @Valid NewProjectForm newProject) {
        ProjectTable projectTable = projectFactory.createProject(newProject);
        return ResponseEntity.ok(projectTable.getUrl());
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @GetMapping("/project/{author}/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<HangarProject> getHangarProject(@PathVariable String author, @PathVariable String slug) {
        return ResponseEntity.ok(projectService.getHangarProject(author, slug));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#author, #slug}")
    @PostMapping(path = "/project/{author}/{slug}/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveProjectSettings(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody ProjectSettingsForm settingsForm) {
        projectService.saveSettings(author, slug, settingsForm);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
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
    @PostMapping(path = "/project/{author}/{slug}/rename", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renameProject(@PathVariable String author, @PathVariable String slug, @Valid @RequestBody StringContent nameContent) {
        return ResponseEntity.ok(projectFactory.renameProject(author, slug, nameContent.getContent()));
    }

    @Unlocked
    @VisibilityRequired(type = Type.PROJECT, args = "{#projectId}")
    @PostMapping("/project/{id}/star/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectStarred(@PathVariable("id") long projectId, @PathVariable boolean state) {
        userService.toggleStarred(projectId, state);
    }

    @Unlocked
    @VisibilityRequired(type = Type.PROJECT, args = "{#projectId}")
    @PostMapping("/project/{id}/watch/{state}")
    @ResponseStatus(HttpStatus.OK)
    public void setProjectWatching(@PathVariable("id") long projectId, @PathVariable boolean state) {
        userService.toggleWatching(projectId, state);
    }

//    @GetMapping("/project/{author}/{name}/flags")
//    public void getProjectFlags(@PathVariable String author, @PathVariable String name) {
//
//    }
}
