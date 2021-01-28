package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.modelold.Color;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.security.annotations.ProjectPermission;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.serviceold.project.ChannelService;
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
    private final ObjectMapper mapper;

    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;

    @Autowired
    public ChannelsController(ChannelService channelService, ObjectMapper mapper, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.channelService = channelService;
        this.mapper = mapper;
        this.projectsTable = projectsTable;
        this.projectData = projectData;
    }

    @ProjectPermission(NamedPermission.EDIT_TAGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/channels")
    public ModelAndView showList(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/channels/list");
        ProjectData projData = projectData.get();
        ArrayNode channels = mapper.createArrayNode();
        channelService.getChannelsWithVersionCount(projData.getProject().getId()).forEach((projectChannelsTable, count) -> {
            ObjectNode channel = mapper.createObjectNode()
                    .put("versionCount", count)
                    .set("channel", mapper.valueToTree(projectChannelsTable));
            channels.add(channel);
        });
        mv.addObject("p", projData);
        mv.addObject("channels", channels);
        return fillModel(mv);
    }

    @ProjectPermission(NamedPermission.EDIT_TAGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels")
    public ModelAndView create(@PathVariable String author, @PathVariable String slug, @RequestParam("name") String channelId, @RequestParam("color") Color channelColor, @RequestParam("nonReviewed") boolean nonReviewed) {
        channelService.addProjectChannel(projectsTable.get().getId(), channelId, channelColor, nonReviewed);
        return Routes.CHANNELS_SHOW_LIST.getRedirect(author, slug);
    }

    @ProjectPermission(NamedPermission.EDIT_TAGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels/{channel}")
    public ModelAndView save(@PathVariable String author,
                             @PathVariable String slug,
                             @PathVariable String channel,
                             @RequestParam("name") String name,
                             @RequestParam("color") Color color,
                             @RequestParam(value = "nonReviewed") boolean nonReviewed) {
        channelService.updateProjectChannel(projectsTable.get().getId(), channel, name, color, nonReviewed);
        return Routes.CHANNELS_SHOW_LIST.getRedirect(author, slug);
    }

    @ProjectPermission(NamedPermission.EDIT_TAGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/channels/{channel}/delete")
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, @PathVariable String channel) {
        // TODO implement delete request controller
        return Routes.CHANNELS_SHOW_LIST.getRedirect(author, slug);
    }

}

