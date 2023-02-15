package io.papermc.hangar.controller.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.requests.FlagForm;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.admin.ReportNotificationForm;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import io.papermc.hangar.model.internal.projects.HangarProjectFlagNotification;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.admin.FlagService;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@LoggedIn
@Controller
@RateLimit(path = "flag")
@RequestMapping("/api/internal/flags")
public class FlagController extends HangarComponent {

    private final FlagService flagService;

    public FlagController(final FlagService flagService) {
        this.flagService = flagService;
    }

    @Unlocked
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void flag(@RequestBody @Valid final FlagForm form) {
        this.flagService.createFlag(form.projectId(), form.reason(), form.comment());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/resolve/{resolve}")
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public void resolve(@PathVariable final long id, @PathVariable final boolean resolve) {
        this.flagService.markAsResolved(id, resolve);
    }

    @ResponseBody
    @GetMapping(path = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public List<HangarProjectFlag> getFlags(@PathVariable final long projectId) {
        return this.flagService.getFlags(projectId);
    }

    @ResponseBody
    @GetMapping(path = "/resolved", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public PaginatedResult<HangarProjectFlag> getResolvedFlags(final @NotNull RequestPagination pagination) {
        return this.flagService.getFlags(pagination, true);
    }

    @ResponseBody
    @GetMapping(path = "/unresolved", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public PaginatedResult<HangarProjectFlag> getUnresolvedFlags(final @NotNull RequestPagination pagination) {
        return this.flagService.getFlags(pagination, false);
    }

    @GetMapping(path = "/unresolvedamount")
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public ResponseEntity<Long> getUnresolvedFlagsQueueSize() {
        return ResponseEntity.ok(this.flagService.getFlagsQueueSize(false));
    }

    @ResponseBody
    @GetMapping(path = "/{id}/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public List<HangarProjectFlagNotification> getNotifications(@PathVariable final long id) {
        return this.flagService.getFlagNotifications(id);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    @PostMapping(path = "/{id}/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void notifyReportParty(@PathVariable final long id, @RequestBody final ReportNotificationForm form) {
        this.flagService.notifyParty(id, form.warning(), form.toReporter(), form.content());
    }
}
