package io.papermc.hangar.controller;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import io.papermc.hangar.service.internal.SitemapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

class SitemapControllerTest extends ControllerTest {

    @Autowired
    private SitemapService sitemapService;

    @Test
    void sitemapIndex() throws Exception {
        this.mockMvc.perform(get("/sitemap.xml"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/xml;charset=UTF-8"))
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/global-sitemap.xml')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/TestAdmin/sitemap.xml')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/sitemap.xml')]").exists());
    }

    @Test
    void totalSitemap() throws Exception {
        sitemapService.updateTotalSitemap(); // make sure its updated
        this.mockMvc.perform(get("/total-sitemap.xml"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/xml;charset=UTF-8"))
            // global
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/authors')]").exists())
            // user
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/TestProject')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/TestProject/pages/testparentpage/testchild')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/TestProject/versions/1.0')]").exists());
    }

    @Test
    void globalSitemap() throws Exception {
        this.mockMvc.perform(get("/global-sitemap.xml"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/xml;charset=UTF-8"))
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/authors')]").exists());
    }

    @Test
    void userSitemap() throws Exception {
        this.mockMvc.perform(get("/" + TestData.ORG.getName() + "/sitemap.xml"))
            .andExpect(status().is(200))
            .andExpect(content().contentType("application/xml;charset=UTF-8"))
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/TestProject')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/TestProject/pages/testparentpage/testchild')]").exists())
            .andExpect(xpath("//loc[contains(text(), 'http://localhost:3333/PaperMC/TestProject/versions/1.0')]").exists());
    }

    @Test
    void userSitemapUnknown() throws Exception {
        this.mockMvc.perform(get("/dum/sitemap.xml"))
            .andExpect(status().is(404));
    }
}
