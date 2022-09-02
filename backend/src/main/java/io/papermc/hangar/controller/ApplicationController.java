package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.RobotsBuilder;
import io.papermc.hangar.controller.extras.StatusZ;
import io.papermc.hangar.security.annotations.Anyone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Anyone
@Controller
public class ApplicationController extends HangarComponent {

    private final StatusZ statusZ;

    @Autowired
    public ApplicationController(StatusZ statusZ) {
        this.statusZ = statusZ;
    }

    @ResponseBody
    @GetMapping("/statusz")
    public ObjectNode showStatusZ() {
        return statusZ.getStatus();
    }

    @GetMapping(path = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return new RobotsBuilder()
                .group("*")
                .disallow("/invalidate")
                .disallow("/login")
                .disallow("/logout")
                .disallow("/handle-logout")
                .disallow("/logged-out")
                .disallow("/refresh")
                .disallow("/signup")
                .disallow("/verify")
                .disallow("/linkout")
                .disallow("/admin")
                .disallow("/actuator")
                .disallow("/error")
                .disallow("/statusz")
                .disallow("/api")
                .allow("/api$")
                .disallow("/notifications")
                .disallow("/*/settings")
                .disallow("/*/*/channels")
                .disallow("/*/*/discuss")
                .disallow("/*/*/flags")
                .disallow("/*/*/notes")
                .disallow("/*/*/settings")
                .disallow("/*/*/stars")
                .disallow("/*/*/watchers")
                .disallow("/*/*/versions/new$")
                .disallow("/*/*/versions/*/download")
                .disallow("/*/*/versions/*/jar")
                .disallow("/*/*/versions/*/confirm")
                .disallow("/*/*/versions/*/*/reviews")
                .endGroup()
                .sitemap(config.getBaseUrl() + "/sitemap.xml")
                .build();
    }
}
