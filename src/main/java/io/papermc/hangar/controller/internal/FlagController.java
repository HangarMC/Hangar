package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.api.requests.FlagForm;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.service.internal.admin.FlagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;

@LoggedIn
@Controller
@RequestMapping("/api/internal/flags")
public class FlagController extends HangarController {

    private final FlagService flagService;

    public FlagController(FlagService flagService) {
        this.flagService = flagService;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void flag(@RequestBody @Valid FlagForm form) {
        flagService.createFlag(form.getProjectId(), form.getReason(), form.getComment());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/resolve/{resolve}")
    @PermissionRequired(perms = NamedPermission.MOD_NOTES_AND_FLAGS)
    public void resolve(@PathVariable long id, @PathVariable boolean resolve) {
        flagService.markAsResolved(id, resolve);
    }

    @ResponseBody
    @GetMapping(value = "/{author}/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(perms = NamedPermission.MOD_NOTES_AND_FLAGS)
    public List<HangarProjectFlag> getFlags(@PathVariable String author, @PathVariable String slug) {
        return flagService.getFlags(author, slug);
    }

    @ResponseBody
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(perms = NamedPermission.MOD_NOTES_AND_FLAGS)
    public List<HangarProjectFlag> getFlags() {
        return flagService.getFlags();
    }
}
