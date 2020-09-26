package io.papermc.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import cz.jiripinkas.jsitemapgenerator.ChangeFreq;
import cz.jiripinkas.jsitemapgenerator.WebPage;
import cz.jiripinkas.jsitemapgenerator.generator.SitemapGenerator;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.ProjectPageDao;
import io.papermc.hangar.db.dao.ProjectVersionDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.viewhelpers.ProjectPage;

@Service
public class SitemapService {

    private final HangarConfig hangarConfig;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<ProjectVersionDao> versionDao;
    private final HangarDao<ProjectPageDao> pageDao;

    @Autowired
    public SitemapService(HangarConfig hangarConfig, HangarDao<UserDao> userDao, HangarDao<ProjectDao> projectDao, HangarDao<ProjectVersionDao> versionDao, HangarDao<ProjectPageDao> pageDao) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.projectDao = projectDao;
        this.versionDao = versionDao;
        this.pageDao = pageDao;
    }

    @Cacheable("indexSitemap")
    public String getSitemap() {
        SitemapGenerator generator = SitemapGenerator.of(hangarConfig.getBaseUrl())
                .addPage(WebPage.builder().name("global-sitemap.xml").build());

        userDao.get().getAllAuthorNames().forEach(user -> generator.addPage(user + "/sitemap.xml"));

        return generator.toString();
    }

    @Cacheable("globalSitemap")
    public String getGlobalSitemap() {
        return SitemapGenerator.of(hangarConfig.getBaseUrl())
                .addPage(WebPage.builder().name("").changeFreq(ChangeFreq.HOURLY).build())
                .addPage(WebPage.builder().name("authors").changeFreq(ChangeFreq.WEEKLY).build())
                .addPage(WebPage.builder().name("api").build())
                .toString();
    }

    @Cacheable(value = "userSitemap", key = "#user.name")
    public String getUserSitemap(UsersTable user) {
        SitemapGenerator generator = SitemapGenerator.of(hangarConfig.getBaseUrl());

        // add all projects
        List<ProjectsTable> projects = projectDao.get().getProjectsByUserId(user.getId());
        projects.forEach(p -> generator.addPage(user.getName() + "/" + p.getSlug()));

        // add all versions of said projects
        projects.forEach(p -> {
            List<ProjectVersionsTable> versions = versionDao.get().getProjectVersions(p.getId());
            versions.forEach(v -> generator.addPage(user.getName() + "/" + p.getSlug() + "/" + v.getVersionString()));
        });

        // add all pages of said projects
        projects.forEach(project -> {
            List<ProjectPage> pages = pageDao.get().getPages(project.getOwnerName(), project.getSlug());
            pages.forEach(page -> generator.addPage(user.getName() + "/" + project.getSlug() + "/" + page.getSlug()));
        });

        // lastly, add user page
        generator.addPage(WebPage.builder().name(user.getName()).changeFreq(ChangeFreq.WEEKLY).build());

        return generator.toString();
    }
}
