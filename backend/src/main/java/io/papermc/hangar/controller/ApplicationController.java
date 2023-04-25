package io.papermc.hangar.controller;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.RobotsBuilder;
import io.papermc.hangar.security.annotations.Anyone;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Anyone
@Controller
public class ApplicationController extends HangarComponent {

    @GetMapping(path = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        if (!this.config.isAllowIndexing()) {
            return new RobotsBuilder()
                .group("*")
                .disallow("/")
                .endGroup()
                .build();
        }
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
            .disallow("/version-info")
            .disallow("/api")
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
            .sitemap(this.config.getBaseUrl() + "/sitemap.xml")
            .build();
    }
}
