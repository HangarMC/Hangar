package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.internal.projects.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Anyone
@Controller
@RequestMapping(value = "/api/internal/channels", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChannelController extends HangarController {

    private final ChannelService channelService;

    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#projectId}")
    @GetMapping("/all/{id}")
    public ResponseEntity<List<ProjectChannelTable>> getProjectChannels(@PathVariable("id") long projectId) {
        return ResponseEntity.ok(channelService.getProjectChannels(projectId));
    }
}
