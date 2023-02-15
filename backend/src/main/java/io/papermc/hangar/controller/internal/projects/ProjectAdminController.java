package io.papermc.hangar.controller.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.projects.VisibilityChangeForm;
import io.papermc.hangar.model.internal.projects.HangarProjectNote;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.projects.ProjectAdminService;
import io.papermc.hangar.service.internal.projects.ProjectNoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @el(projectId: long)
@Unlocked
@Controller
@RateLimit(path = "projectadmin")
@RequestMapping("/api/internal/projects")
public class ProjectAdminController extends HangarComponent {

    private final ProjectAdminService projectAdminService;
    private final ProjectNoteService projectNoteService;

    @Autowired
    public ProjectAdminController(final ProjectAdminService projectAdminService, final ProjectNoteService projectNoteService) {
        this.projectAdminService = projectAdminService;
        this.projectNoteService = projectNoteService;
    }

    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    @GetMapping(path = "/notes/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HangarProjectNote>> getProjectNotes(@PathVariable final long projectId) {
        return ResponseEntity.ok(this.projectNoteService.getNotes(projectId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    @PostMapping(path = "/notes/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addProjectNote(@PathVariable final long projectId, @RequestBody @Valid final StringContent content) {
        this.projectNoteService.addNote(projectId, content.getContent());
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.REVIEWER)
    @PostMapping(path = "/visibility/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void changeProjectVisibility(@PathVariable final long projectId, @RequestBody @Valid final VisibilityChangeForm visibilityChangeForm) {
        this.projectAdminService.changeVisibility(projectId, visibilityChangeForm.visibility(), visibilityChangeForm.comment());
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_PAGE, args = "{#projectId}")
    @PostMapping("/visibility/{projectId}/sendforapproval")
    public void sendProjectForApproval(@PathVariable final long projectId) {
        this.projectAdminService.sendProjectForApproval(projectId);
    }
}
