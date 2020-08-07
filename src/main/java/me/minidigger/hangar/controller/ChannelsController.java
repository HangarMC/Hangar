package me.minidigger.hangar.controller;

import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.service.project.ChannelService;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.RouteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ChannelsController extends HangarController {

    private final ProjectService projectService;
    private final ChannelService channelService;
    private final RouteHelper routeHelper;

    @Autowired
    public ChannelsController(ProjectService projectService, ChannelService channelService, RouteHelper routeHelper) {
        this.projectService = projectService;
        this.channelService = channelService;
        this.routeHelper = routeHelper;
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/channels")
    public ModelAndView showList(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/channels/list");
        ProjectData projectData = projectService.getProjectData(author, slug);
        mv.addObject("p", projectData);
        mv.addObject("channels", channelService.getProjectChannels(projectData.getProject().getId()));
        mv.addObject("versions", projectData.getPublicVersions());
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels")
    public ModelAndView create(@PathVariable String author, @PathVariable String slug) {
        // TODO implement create request controller
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("channels.showList", author, slug));
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels/{channel}")
    public ModelAndView save(@PathVariable String author, @PathVariable String slug, @PathVariable String channel) {
        // TODO implement save request controller
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("channels.showList", author, slug));
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels/{channel}/delete")
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, @PathVariable String channel) {
        // TODO implement delete request controller
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("channels.showList", author, slug));
    }

}

