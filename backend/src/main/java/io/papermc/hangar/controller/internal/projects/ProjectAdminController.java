package io.papermc.hangar.controller.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.VisibilityChangeForm;
import io.papermc.hangar.model.internal.projects.HangarProjectNote;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.projects.ProjectAdminService;
import io.papermc.hangar.service.internal.projects.ProjectNoteService;
import java.util.List;
import javax.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;

// @el(projectId: long)
@LoggedIn
@Controller
@RateLimit(path = "projectadmin")
@RequestMapping("/api/internal/projects")
public class ProjectAdminController extends HangarComponent {

    private final ProjectAdminService projectAdminService;
    private final ProjectNoteService projectNoteService;

    @Autowired
    public ProjectAdminController(ProjectAdminService projectAdminService, ProjectNoteService projectNoteService) {
        this.projectAdminService = projectAdminService;
        this.projectNoteService = projectNoteService;
    }

    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    @GetMapping(path = "/notes/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HangarProjectNote>> getProjectNotes(@PathVariable long projectId) {
        return ResponseEntity.ok(projectNoteService.getNotes(projectId));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.CREATED)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    @PostMapping(path = "/notes/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addProjectNote(@PathVariable long projectId, @RequestBody @Valid StringContent content) {
        projectNoteService.addNote(projectId, content.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.REVIEWER)
    @PostMapping(path = "/visibility/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void changeProjectVisibility(@PathVariable long projectId, @Valid @RequestBody VisibilityChangeForm visibilityChangeForm) {
        projectAdminService.changeVisibility(projectId, visibilityChangeForm.getVisibility(), visibilityChangeForm.getComment());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_PAGE, args = "{#projectId}")
    @PostMapping("/visibility/{projectId}/sendforapproval")
    public void sendProjectForApproval(@PathVariable long projectId) {
        projectAdminService.sendProjectForApproval(projectId);
    }
}
