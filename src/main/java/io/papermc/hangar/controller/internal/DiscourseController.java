package io.papermc.hangar.controller.internal;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    // TODO implement

    @GetMapping("/createPost")
    @ResponseBody
    public String createPost() {
        discourseService.createPost("admin", 15, "This is a test. It needs to be 20 characters long, so I writing some bullshit here 2");
        return "dum";
    }

    @GetMapping("/createTopic")
    @ResponseBody
    public String createTopic() {
        discourseService.createTopic("admin", "This is a test title that might be ok?", "This is a test. It needs to be 20 characters long, so I writing some bullshit here 2", null);
        return "dum";
    }

    @GetMapping("/updateTopic")
    @ResponseBody
    public String updateTopic() {
        discourseService.updateTopic("admin", 15, "This is a test that might be ok? 2", null);
        return "dum";
    }

    @GetMapping("/updatePost")
    @ResponseBody
    public String updatePost() {
        discourseService.updatePost("admin", 28, "This is a test. It needs to be 20 characters long, so I writing some bullshit here 5");
        return "dum";
    }

    @GetMapping("/deleteTopic")
    @ResponseBody
    public String deleteTopic() {
        discourseService.deleteTopic("admin", 15);
        return "dum";
    }
}
