package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.internal.projects.ChannelService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final ProjectService projectService;

    @Autowired
    public ChannelController(ChannelService channelService, ProjectService projectService) {
        this.channelService = channelService;
        this.projectService = projectService;
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @GetMapping(path = "/{author}/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProjectChannelTable>> getProjectChannels(@PathVariable String author, @PathVariable String slug) {
        ProjectTable projectTable = projectService.getProjectTable(author, slug);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(channelService.getProjectChannels(projectTable.getId()));
    }
}
