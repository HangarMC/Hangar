package io.papermc.hangar.controller;

import io.papermc.hangar.service.internal.SitemapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SitemapController extends HangarController {

    private final SitemapService sitemapService;

    @Autowired
    public SitemapController(SitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemapIndex() {
        return sitemapService.getSitemap();
    }

    @GetMapping(value = "/global-sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String globalSitemap() {
        return sitemapService.getGlobalSitemap();
    }

    @GetMapping(value = "/{user}/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String userSitemap(@PathVariable String user) {
        return sitemapService.getUserSitemap(user);
    }
}
