package io.papermc.hangar.service.internal;

import cz.jiripinkas.jsitemapgenerator.ChangeFreq;
import cz.jiripinkas.jsitemapgenerator.WebPage;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapGenerator;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapIndexGenerator;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectPagesDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SitemapService extends HangarComponent {

    private final UserDAO userDAO;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final HangarProjectPagesDAO hangarProjectPagesDAO;

    public SitemapService(final UserDAO userDAO, final ProjectsDAO projectsDAO, final ProjectVersionsDAO projectVersionsDAO, final HangarProjectPagesDAO hangarProjectPagesDAO) {
        this.userDAO = userDAO;
        this.projectsDAO = projectsDAO;
        this.projectVersionsDAO = projectVersionsDAO;
        this.hangarProjectPagesDAO = hangarProjectPagesDAO;
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
        return SitemapGenerator.of(this.config.getBaseUrl())
            .addPage(WebPage.builder().name("").changeFreq(ChangeFreq.HOURLY).build())
            .addPage(WebPage.builder().name("authors").changeFreq(ChangeFreq.WEEKLY).build())
            .addPage(WebPage.builder().name("staff").changeFreq(ChangeFreq.WEEKLY).build())
            .addPage(WebPage.builder().name("guidelines").changeFreq(ChangeFreq.WEEKLY).build())
            .addPage(WebPage.builder().name("terms").changeFreq(ChangeFreq.WEEKLY).build())
            .addPage(WebPage.builder().name("privacy").changeFreq(ChangeFreq.WEEKLY).build())
            .addPage(WebPage.builder().name("version").changeFreq(ChangeFreq.DAILY).build())
            .addPage(WebPage.builder().name("api-docs").build())
            .toString();
    }

    @Cacheable(value = CacheConfig.USER_SITEMAP, key = "#username")
    public String getUserSitemap(final String username) {
        final UserTable userTable = this.userDAO.getUserTable(username);
        if (userTable == null) {
            throw HangarApiException.notFound();
        }

        final SitemapGenerator generator = SitemapGenerator.of(this.config.getBaseUrl());
        generator.defaultChangeFreqWeekly();

        // add all projects
        final List<ProjectTable> projects = this.projectsDAO.getUserProjects(userTable.getId(), false);
        projects.removeIf(p -> p.getVisibility() != Visibility.PUBLIC && p.getVisibility() != Visibility.NEEDSAPPROVAL);
        projects.forEach(p -> generator.addPage(userTable.getName() + "/" + p.getSlug()));

        // add all versions of said projects
        projects.forEach(p -> {
            final List<ProjectVersionTable> projectVersions = this.projectVersionsDAO.getProjectVersions(p.getId());
            projectVersions.stream()
                .filter(pv -> pv.getVisibility() == Visibility.PUBLIC || pv.getVisibility() == Visibility.NEEDSAPPROVAL)
                .forEach(pv -> generator.addPage(this.path(userTable.getName(), p.getSlug(), "versions", pv.getVersionString())));
        });

        // add all pages of said projects
        projects.forEach(project -> {
            final List<ExtendedProjectPage> projectPages = this.hangarProjectPagesDAO.getProjectPages(project.getId());
            for (final ExtendedProjectPage pp : projectPages) {
                if (pp.isHome()) {
                    continue;
                }
                generator.addPage(this.path(userTable.getName(), project.getSlug(), "pages", pp.getSlug()));
            }
        });

        // lastly, add user page
        generator.addPage(WebPage.builder().name(userTable.getName()).build());

        return generator.toString();
    }

    private String path(final String... paths) {
        return String.join("/", paths);
    }
}
