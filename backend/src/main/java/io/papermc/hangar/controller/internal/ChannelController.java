package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.projects.ChannelForm;
import io.papermc.hangar.model.internal.api.requests.projects.EditChannelForm;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.internal.projects.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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

// @el(projectId: long)
// @el(project: ProjectTable)
@RestController
@RateLimit(path = "channel")
@RequestMapping(value = "/api/internal/channels", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChannelController extends HangarComponent {

    private final ChannelService channelService;

    @Autowired
    public ChannelController(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping("/checkName")
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_CHANNEL, args = "{#projectId}")
    public void checkName(@RequestParam final long projectId, @RequestParam final String name, @RequestParam(required = false) final String existingName) {
        this.channelService.checkName(projectId, name, existingName);
    }

    @GetMapping("/checkColor")
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_CHANNEL, args = "{#projectId}")
    public void checkColor(@RequestParam final long projectId, @RequestParam final Color color, @RequestParam(required = false) final Color existingColor) {
        this.channelService.checkColor(projectId, color, existingColor);
    }

    // @el(author: String, slug: String)
    @Anyone
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    @GetMapping(path = "/{project}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HangarChannel>> getChannels(@PathVariable final ProjectTable project) {
        return ResponseEntity.ok(this.channelService.getProjectChannels(project.getId()));
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 15)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_CHANNEL, args = "{#projectId}")
    @PostMapping(path = "/{projectId}/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createChannel(@PathVariable final long projectId, @RequestBody final @Valid ChannelForm channelForm) {
        final Set<ChannelFlag> flags = channelForm.getFlags();
        flags.retainAll(ChannelFlag.EDITABLE);
        this.channelService.createProjectChannel(channelForm.getName(), channelForm.getDescription(), channelForm.getColor(), projectId, flags);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 15)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_CHANNEL, args = "{#projectId}")
    @PostMapping(path = "/{projectId}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editChannel(@PathVariable final long projectId, @RequestBody final @Valid EditChannelForm channelForm) {
        final Set<ChannelFlag> flags = channelForm.getFlags();
        flags.retainAll(ChannelFlag.EDITABLE);
        this.channelService.editProjectChannel(channelForm.getId(), channelForm.getName(), channelForm.getDescription(), channelForm.getColor(), projectId, flags);
    }

    @Unlocked
    @RequireAal(1)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_CHANNEL, args = "{#project}")
    @PostMapping("/{project}/delete/{channel}")
    public void deleteChannel(@PathVariable final ProjectTable project, @PathVariable final ProjectChannelTable channel) {
        this.channelService.deleteProjectChannel(project.getProjectId(), channel.getId());
    }
}
