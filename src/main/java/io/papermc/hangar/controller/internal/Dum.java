package io.papermc.hangar.controller.internal;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.papermc.hangar.service.internal.projects.HomeProjectService;

@Controller
@RequestMapping(value = "/api/internal/dum", produces = MediaType.APPLICATION_JSON_VALUE)
public class Dum {

    private final HomeProjectService homeProjectService;

    public Dum(HomeProjectService homeProjectService) {
        this.homeProjectService = homeProjectService;
    }

    @RequestMapping("/")
    @ResponseBody
    public String dum() {
        homeProjectService.refreshHomeProjects();
        return "dum";
    }
}
