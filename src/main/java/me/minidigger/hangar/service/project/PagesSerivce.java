package me.minidigger.hangar.service.project;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectPageDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.util.StringUtils;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PagesSerivce {

    private final HangarDao<ProjectPageDao> projectPageDao;

    @Autowired
    public PagesSerivce(HangarDao<ProjectPageDao> projectPageDao) {
        this.projectPageDao = projectPageDao;
    }

    public ProjectPage getPage(long projectId, String pageName) {
        return projectPageDao.get().getPage(projectId, StringUtils.slugify(pageName), null);
    }

    public List<ProjectPage> getPages(String pluginId) {
        return projectPageDao.get().getPages(pluginId);
    }

    /**
     * Gets a page parents. Must specified either pageName or pageId
     * @param projectId project Id
     * @param pageName pages name
     * @param pageId page id
     * @return List of page parents
     */
    public List<ProjectPage> getPageParents(long projectId, @Nullable String pageName, @Nullable Long pageId) {
        Preconditions.checkArgument(pageName == null && pageId == null, "One of (pageName, pageId) must be nonnull!");
        return projectPageDao.get().getPageParents(projectId, pageName, pageId);
    }

    public void fillPages(ModelAndView mav, long projectId) {
        AtomicInteger pageCount = new AtomicInteger();
        Map<Long, ProjectPage> rootPages = projectPageDao.get().getRootPages(projectId);
        pageCount.addAndGet(rootPages.size());
        Map<ProjectPage, List<ProjectPage>> projectPages = new LinkedHashMap<>(); // need linked to preserve page order
        rootPages.forEach((id, rootPage) -> {
            List<ProjectPage> childPages = projectPageDao.get().getChildPages(projectId, id);
            pageCount.addAndGet(childPages.size());
            projectPages.put(rootPage, childPages);
        });

        mav.addObject("rootPages", projectPages);
        mav.addObject("pageCount", pageCount.get());
    }
}

