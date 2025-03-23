package io.papermc.hangar.service.internal;

import cz.jiripinkas.jsitemapgenerator.WebPage;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapGenerator;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapIndexGenerator;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectPagesDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SitemapService extends HangarComponent {

    private static final Logger logger = LoggerFactory.getLogger(SitemapService.class);

    private final UserDAO userDAO;
    private final ProjectsDAO projectsDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final HangarProjectPagesDAO hangarProjectPagesDAO;

    private String totalSitemap;

    public SitemapService(final UserDAO userDAO, final ProjectsDAO projectsDAO, final VersionsApiDAO versionsApiDAO, final HangarProjectPagesDAO hangarProjectPagesDAO) {
        this.userDAO = userDAO;
        this.projectsDAO = projectsDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.hangarProjectPagesDAO = hangarProjectPagesDAO;
    }

    @Scheduled(fixedDelayString = "PT6H", initialDelayString = "PT1S")
    public void updateTotalSitemap() {
        logger.info("Updating sitemap...");
        LocalDateTime start = LocalDateTime.now();
        SitemapGenerator generator = SitemapGenerator.of(this.config.getBaseUrl());
        addGlobal(generator);
        this.userDAO.getUsers().forEach((u) -> addUser(u, generator));
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
        final SitemapIndexGenerator generator = SitemapIndexGenerator.of(this.config.getBaseUrl())
            .addPage(WebPage.builder().name("global-sitemap.xml").build());

        this.userDAO.getAuthorNames().forEach(name -> generator.addPage(name + "/sitemap.xml"));
        return generator.toString();
    }

    @Cacheable(CacheConfig.GLOBAL_SITEMAP)
    public String getGlobalSitemap() {
        return addGlobal(SitemapGenerator.of(this.config.getBaseUrl())).toString();
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
        final UserTable userTable = this.userDAO.getUserTable(username);
        if (userTable == null) {
            throw HangarApiException.notFound();
        }

        return addUser(userTable, SitemapGenerator.of(this.config.getBaseUrl())).toString();
    }

    private SitemapGenerator addUser(UserTable userTable, SitemapGenerator generator) {
        // add all projects
        final List<ProjectTable> projects = this.projectsDAO.getUserProjects(userTable.getId(), false);
        projects.removeIf(p -> p.getVisibility() != Visibility.PUBLIC && p.getVisibility() != Visibility.NEEDSAPPROVAL);
        projects.forEach(p -> generator.addPage(WebPage.builder().name(userTable.getName() + "/" + p.getSlug()).changeFreqWeekly().priority(0.9).build()));

        // add all versions of said projects
        projects.forEach(p -> {
            // TODO version compact, we dont need downloads here
            // TODO sitemap dao, move to components, then remove the method in the dao
            final SortedMap<Long, Version> projectVersions = this.versionsApiDAO.getVersions(p.getId(), false, null, new RequestPagination(100L, 0L));
            projectVersions.values().stream()
                .filter(pv -> pv.getVisibility() == Visibility.PUBLIC || pv.getVisibility() == Visibility.NEEDSAPPROVAL)
                .forEach(pv -> generator.addPage(WebPage.builder().name(this.path(userTable.getName(), p.getSlug(), "versions", pv.getName()))
                    .priority(pv.getChannel().getName().equals(this.config.channels.nameDefault()) ? 0.5 : 0.1)
                    .changeFreqMonthly()
                    .build())
                );
        });

        // add all pages of said projects
        projects.forEach(project -> {
            final List<ExtendedProjectPage> projectPages = this.hangarProjectPagesDAO.getProjectPages(project.getId());
            for (final ExtendedProjectPage pp : projectPages) {
                if (pp.isHome()) {
                    continue;
                }
                generator.addPage(WebPage.builder().name(this.path(userTable.getName(), project.getSlug(), "pages", pp.getSlug())).priority(0.5).changeFreqWeekly().build());
            }
        });

        // lastly, add user page
        generator.addPage(WebPage.builder().name(userTable.getName()).priority(projects.isEmpty() ? 0.1 : 0.4).changeFreqWeekly().build());

        return generator;
    }

    private String path(final String... paths) {
        return String.join("/", paths);
    }
}
