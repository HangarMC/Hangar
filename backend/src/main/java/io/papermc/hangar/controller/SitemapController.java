package io.papermc.hangar.controller;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.service.internal.SitemapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SitemapController extends HangarComponent {

    private final SitemapService sitemapService;

    @Autowired
    public SitemapController(final SitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    @Anyone
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemapIndex() {
        return this.sitemapService.getSitemap();
    }

    @Anyone
    @GetMapping(value = "/global-sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String globalSitemap() {
        return this.sitemapService.getGlobalSitemap();
    }

    @Anyone
    @GetMapping(value = "/{user}/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String userSitemap(@PathVariable final String user) {
        return this.sitemapService.getUserSitemap(user);
    }
}
