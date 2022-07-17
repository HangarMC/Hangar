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
import java.util.List;
import javax.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    public void flag(@RequestBody @Valid FlagForm form) {
        flagService.createFlag(form.getProjectId(), form.getReason(), form.getComment());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/resolve/{resolve}")
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public void resolve(@PathVariable long id, @PathVariable boolean resolve) {
        flagService.markAsResolved(id, resolve);
    }

    @ResponseBody
    @GetMapping(path = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public List<HangarProjectFlag> getFlags(@PathVariable long projectId) {
        return flagService.getFlags(projectId);
    }

    @ResponseBody
    @GetMapping(path = "/resolved", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public PaginatedResult<HangarProjectFlag> getResolvedFlags(@NotNull RequestPagination pagination) {
        return flagService.getFlags(pagination, true);
    }

    @ResponseBody
    @GetMapping(path = "/unresolved", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public PaginatedResult<HangarProjectFlag> getUnresolvedFlags(@NotNull RequestPagination pagination) {
        return flagService.getFlags(pagination, false);
    }

    @GetMapping(path = "/unresolvedamount")
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public ResponseEntity<Long> getUnresolvedFlagsQueueSize() {
        return ResponseEntity.ok(flagService.getFlagsQueueSize(false));
    }

    @ResponseBody
    @GetMapping(path = "/{id}/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    public List<HangarProjectFlagNotification> getNotifications(@PathVariable long id) {
        return flagService.getFlagNotifications(id);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.MOD_NOTES_AND_FLAGS)
    @PostMapping(path = "/{id}/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void notifyReportParty(@PathVariable long id, @RequestBody ReportNotificationForm form) {
        flagService.notifyParty(id, form.warning(), form.toReporter(), form.content());
    }
}
