package io.papermc.hangar.controller.internal;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.service.internal.discourse.DiscourseService;

@Controller
@RequestMapping(value = "/api/internal/discourse", produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscourseController extends HangarComponent {

    private final DiscourseService discourseService;

    public DiscourseController(DiscourseService discourseService) {
        this.discourseService = discourseService;
    }

    @PostMapping("/{projectId}/comment")
    @ResponseBody
    public String createPost(@PathVariable("projectId") long projectId, @RequestBody String content) {
        discourseService.createComment(projectId, getHangarPrincipal().getName(), content);
        return "dum";
    }
}
