package io.papermc.hangar.controller;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.RobotsBuilder;
import io.papermc.hangar.security.annotations.Anyone;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Anyone
@RestController
public class ApplicationController extends HangarComponent {

    @GetMapping(path = "/api")
    public void apiRootRedirect() {
        this.response.setStatus(301);
        this.response.setHeader("Location", "/api-docs");
    }

    @GetMapping(path = "/security.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String security() {
        return """
            Contact: mailto:security@papermc.io
            Expires: 2027-01-01T00:00:00.000Z
            Preferred-Languages: en,de
            Canonical: https://hangar.papermc.io/security.txt
            """;
    }

    @GetMapping(path = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        if (!this.config.allowIndexing()) {
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
            .sitemap(this.config.baseUrl() + "/sitemap.xml")
            .build();
    }
}
