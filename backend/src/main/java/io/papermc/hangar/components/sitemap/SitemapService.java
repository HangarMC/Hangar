package io.papermc.hangar.components.sitemap;

import cz.jiripinkas.jsitemapgenerator.WebPage;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapGenerator;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapIndexGenerator;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.sitemap.model.SitemapProject;
import io.papermc.hangar.components.sitemap.model.SitemapUser;
import io.papermc.hangar.components.sitemap.model.SitemapVersion;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SitemapService extends HangarComponent {

    private static final Logger logger = LoggerFactory.getLogger(SitemapService.class);

    private final SitemapDAO sitemapDAO;

    private String totalSitemap;

    public SitemapService(final SitemapDAO sitemapDAO) {
        this.sitemapDAO = sitemapDAO;
    }

    @Scheduled(fixedDelayString = "PT6H", initialDelayString = "PT1S")
    public void updateTotalSitemap() {
        logger.info("Updating sitemap...");
        LocalDateTime start = LocalDateTime.now();
        SitemapGenerator generator = SitemapGenerator.of(this.config.baseUrl());
        addGlobal(generator);
        this.sitemapDAO.getUsers().forEach((u) -> addUser(u, generator));
        this.totalSitemap = generator.toString();
        logger.info("Updated sitemap, took {}.", Duration.between(start, LocalDateTime.now()));
    }

    public String getTotalSitemap() {
        if (this.totalSitemap == null) {
            throw HangarApiException.notFound();
        }
        return this.totalSitemap;
    }

    @Cacheable(CacheConfig.INDEX_SITEMAP)
    public String getSitemap() {
        final SitemapIndexGenerator generator = SitemapIndexGenerator.of(this.config.baseUrl())
            .addPage(WebPage.builder().name("global-sitemap.xml").build());

        this.sitemapDAO.getUsers().forEach(user -> generator.addPage(user.name() + "/sitemap.xml"));
        return generator.toString();
    }

    @Cacheable(CacheConfig.GLOBAL_SITEMAP)
    public String getGlobalSitemap() {
        return addGlobal(SitemapGenerator.of(this.config.baseUrl())).toString();
    }

    private static SitemapGenerator addGlobal(SitemapGenerator generator) {
        generator
            .addPage(WebPage.builder().name("").changeFreqDaily().priority(1.0).build())
            .addPage(WebPage.builder().name("paper").changeFreqDaily().priority(1.0).build())
            .addPage(WebPage.builder().name("waterfall").changeFreqDaily().priority(1.0).build())
            .addPage(WebPage.builder().name("velocity").changeFreqDaily().priority(1.0).build())
            .addPage(WebPage.builder().name("authors").changeFreqWeekly().priority(1.0).build())
            .addPage(WebPage.builder().name("staff").changeFreqWeekly().priority(1.0).build())
            .addPage(WebPage.builder().name("guidelines").changeFreqMonthly().priority(1.0).build())
            .addPage(WebPage.builder().name("terms").changeFreqMonthly().priority(1.0).build())
            .addPage(WebPage.builder().name("privacy").changeFreqMonthly().priority(1.0).build())
            .addPage(WebPage.builder().name("version").changeFreqWeekly().priority(1.0).build())
            .addPage(WebPage.builder().name("api-docs").changeFreqMonthly().priority(1.0).build());
        return generator;
    }

    @Cacheable(value = CacheConfig.USER_SITEMAP, key = "#username")
    public String getUserSitemap(final String username) {
        final SitemapUser user = this.sitemapDAO.getUser(username);
        if (user == null) {
            throw HangarApiException.notFound();
        }

        return addUser(user, SitemapGenerator.of(this.config.baseUrl())).toString();
    }

    private SitemapGenerator addUser(SitemapUser user, SitemapGenerator generator) {
        final List<SitemapProject> projects = this.sitemapDAO.getProjects(user.id());

        // add user page
        generator.addPage(WebPage.builder().name(user.name()).priority(projects.isEmpty() ? 0.1 : 0.4).changeFreqWeekly().build());

        projects.forEach(project -> {
            // add all projects
            generator.addPage(WebPage.builder().name(user.name() + "/" + project.slug()).changeFreqWeekly().priority(0.9).build());
            // add all versions of said projects
            final List<SitemapVersion> projectVersions = this.sitemapDAO.getVersions(project.id());
            projectVersions.forEach(pv -> generator.addPage(WebPage.builder().name(this.path(user.name(), project.slug(), "versions", pv.versionString()))
                .priority(pv.channel().equals(this.config.channels().nameDefault()) ? 0.5 : 0.1)
                .changeFreqMonthly()
                .build())
            );
            // add all pages of said projects
            final List<String> projectPages = this.sitemapDAO.getPages(project.id());
            projectPages.forEach(page -> {
                generator.addPage(WebPage.builder().name(this.path(user.name(), project.slug(), "pages", page)).priority(0.5).changeFreqWeekly().build());
            });
        });

        return generator;
    }

    private String path(final String... paths) {
        return String.join("/", paths);
    }
}
