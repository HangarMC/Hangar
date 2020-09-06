package io.papermc.hangar.controller;

import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.service.project.ChannelService;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.function.Supplier;

@Controller
public class ChannelsController extends HangarController {

    private final ChannelService channelService;

    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;

    @Autowired
    public ChannelsController(ChannelService channelService, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.channelService = channelService;
        this.projectsTable = projectsTable;
        this.projectData = projectData;
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/channels")
    public ModelAndView showList(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/channels/list");
        mv.addObject("p", projectData.get());
        mv.addObject("channels", channelService.getChannelsWithVersionCount(projectData.get().getProject().getId()));
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels")
    public ModelAndView create(@PathVariable String author, @PathVariable String slug, @RequestParam("channel-input") String channelId, @RequestParam("channel-color-input") Color channelColor) {
        channelService.addProjectChannel(projectsTable.get().getId(), channelId, channelColor);
        return Routes.CHANNELS_SHOW_LIST.getRedirect(author, slug);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels/{channel}")
    public ModelAndView save(@PathVariable String author, @PathVariable String slug, @PathVariable String channel,
                             @RequestParam("channel-input") String newChannelName, @RequestParam("channel-color-input") String newChannelHex) {
        channelService.updateProjectChannel(projectsTable.get().getId(), channel, newChannelName, Color.getByHexStr(newChannelHex));
        return Routes.CHANNELS_SHOW_LIST.getRedirect(author, slug);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels/{channel}/delete")
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, @PathVariable String channel) {
        // TODO implement delete request controller
        return Routes.CHANNELS_SHOW_LIST.getRedirect(author, slug);
    }

}

