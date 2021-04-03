package io.papermc.hangar.service.internal;

import cz.jiripinkas.jsitemapgenerator.ChangeFreq;
import cz.jiripinkas.jsitemapgenerator.WebPage;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapGenerator;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectPagesDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.service.HangarService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SitemapService extends HangarService {

    private final UserDAO userDAO;
    private final ProjectsDAO projectsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final HangarProjectPagesDAO hangarProjectPagesDAO;

    public SitemapService(HangarDao<UserDAO> userDAO, HangarDao<ProjectsDAO> projectsDAO, HangarDao<ProjectVersionsDAO> projectVersionsDAO, HangarDao<HangarProjectPagesDAO> hangarProjectPagesDAO) {
        this.userDAO = userDAO.get();
        this.projectsDAO = projectsDAO.get();
        this.projectVersionsDAO = projectVersionsDAO.get();
        this.hangarProjectPagesDAO = hangarProjectPagesDAO.get();
    }

    @Cacheable("indexSitemap")
    public String getSitemap() {
        SitemapGenerator generator = SitemapGenerator.of(config.getBaseUrl())
                .addPage(WebPage.builder().name("global-sitemap.xml").build());

        userDAO.getAuthorNames().forEach(name -> generator.addPage(name + "/sitemap.xml"));
        return generator.toString();
    }

    @Cacheable("globalSitemap")
    public String getGlobalSitemap() {
        return SitemapGenerator.of(config.getBaseUrl())
                .addPage(WebPage.builder().name("").changeFreq(ChangeFreq.HOURLY).build())
                .addPage(WebPage.builder().name("authors").changeFreq(ChangeFreq.WEEKLY).build())
                .addPage(WebPage.builder().name("staff").changeFreq(ChangeFreq.WEEKLY).build())
                .addPage(WebPage.builder().name("api").build())
                .toString();
    }

    @Cacheable(value = "userSitemap", key = "#username")
    public String getUserSitemap(String username) {
        final UserTable userTable = userDAO.getUserTable(username);
        final SitemapGenerator generator = SitemapGenerator.of(config.getBaseUrl());

        // add all projects
        List<ProjectTable> projects = projectsDAO.getUserProjects(userTable.getId());
        projects.forEach(p -> generator.addPage(userTable.getName() + "/" + p.getSlug()));

        // add all versions of said projects
        projects.forEach(p -> {
            List<ProjectVersionTable> projectVersions = projectVersionsDAO.getProjectVersions(p.getId());

            projectVersions.forEach(pv -> {
                List<Platform> platforms = projectVersionsDAO.getVersionPlatforms(pv.getId());
                platforms.forEach(platform -> generator.addPage(path(userTable.getName(), p.getSlug(), "versions", pv.getVersionString(), platform.name().toLowerCase(Locale.ROOT))));

            });
        });

        // add all pages of said projects
        projects.forEach(project -> {
            List<ExtendedProjectPage> projectPages = hangarProjectPagesDAO.getProjectPages(project.getId());
            for (ExtendedProjectPage pp : projectPages) {
                if (pp.isHome()) {
                    continue;
                }
                generator.addPage(path(userTable.getName(), project.getSlug(), pp.getSlug()));
            }
        });

        // lastly, add user page
        generator.addPage(WebPage.builder().name(userTable.getName()).changeFreq(ChangeFreq.WEEKLY).build());

        return generator.toString();
    }

    private String path(String...paths) {
        return String.join("/", paths);
    }
}
