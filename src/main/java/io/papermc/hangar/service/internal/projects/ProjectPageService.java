package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectPagesDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectPagesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectHomePageTable;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectPage;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.PageContext;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectPageService extends HangarComponent {

    private final ProjectPagesDAO projectPagesDAO;
    private final HangarProjectPagesDAO hangarProjectPagesDAO;
    private final JobService jobService;

    public ProjectPageService(ProjectPagesDAO projectPagesDAO, HangarProjectPagesDAO hangarProjectPagesDAO, JobService jobService) {
        this.projectPagesDAO = projectPagesDAO;
        this.hangarProjectPagesDAO = hangarProjectPagesDAO;
        this.jobService = jobService;
    }

    public void checkDuplicateName(long projectId, String name, @Nullable Long parentId) {
        if ((parentId != null && projectPagesDAO.getChildPage(projectId, parentId, name) != null) || (parentId == null && projectPagesDAO.getRootPage(projectId, StringUtils.slugify(name)) != null)) {
            throw new HangarApiException("page.new.error.duplicateName");
        }
    }

    @Transactional
    public ProjectPageTable createPage(long projectId, String name, String slug, String contents, boolean deletable, @Nullable Long parentId, boolean isHome) {
        if (!isHome && contents.length() < config.pages.getMinLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.minLength");
        }

        if (contents.length() > config.pages.getMaxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        config.pages.testPageName(name);

        checkDuplicateName(projectId, name, parentId);

        ProjectPageTable projectPageTable = new ProjectPageTable(
                projectId,
                name,
                slug,
                contents,
                deletable,
                parentId
        );
        projectPageTable = projectPagesDAO.insert(projectPageTable);
        if (isHome) {
            projectPagesDAO.insertHomePage(new ProjectHomePageTable(projectPageTable.getProjectId(), projectPageTable.getId()));
            jobService.save(new UpdateDiscourseProjectTopicJob(projectId));
        }
        actionLogger.projectPage(LogAction.PROJECT_PAGE_CREATED.create(PageContext.of(projectPageTable.getProjectId(), projectPageTable.getId()), contents, ""));
        return projectPageTable;
    }

    public Map<Long, HangarProjectPage> getProjectPages(long projectId) {
        Map<Long, HangarProjectPage> hangarProjectPages = new LinkedHashMap<>();
        for (ExtendedProjectPage projectPage : hangarProjectPagesDAO.getProjectPages(projectId)) {
            if (projectPage.getParentId() == null) {
                hangarProjectPages.put(projectPage.getId(), new HangarProjectPage(projectPage, projectPage.isHome()));
            } else {
                HangarProjectPage parent = findById(projectPage.getParentId(), hangarProjectPages);
                if (parent == null) {
                    throw new IllegalStateException("Should always find a parent");
                }
                parent.getChildren().put(projectPage.getId(), new HangarProjectPage(projectPage, projectPage.isHome()));
            }
        }

        return hangarProjectPages;
    }

    private HangarProjectPage findById(long id, Map<Long, HangarProjectPage> pageMap) {
        if (pageMap.containsKey(id)) {
            return pageMap.get(id);
        } else {
            for (HangarProjectPage page : pageMap.values()) {
                HangarProjectPage possiblePage = findById(id, page.getChildren());
                if (possiblePage != null) {
                    return possiblePage;
                }
            }
            return null;
        }
    }

    public ExtendedProjectPage getProjectPage(String author, String slug, String requestUri) {
        String path = requestUri.replace("/api/internal/pages/page/" + author + "/" + slug, "");
        ExtendedProjectPage pageTable;
        if (path.equals("") || path.equals("/")) {
            pageTable = hangarProjectPagesDAO.getHomePage(author, slug);
        } else {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            pageTable = hangarProjectPagesDAO.getProjectPage(author, slug, path);
        }
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Page not found");
        }
        return pageTable;
    }

    public ExtendedProjectPage getProjectPage(long id) {
        return hangarProjectPagesDAO.getProjectPage(id);
    }

    @Transactional
    public String createProjectPage(long projectId, NewProjectPage newProjectPage) {
        String slug = StringUtils.slugify(newProjectPage.getName());
        if (newProjectPage.getParentId() != null) {
            slug = projectPagesDAO.getProjectPage(projectId, newProjectPage.getParentId()).getSlug() + "/" + slug;
        }
        createPage(projectId, newProjectPage.getName(), slug, "# " + newProjectPage.getName() + "\n\nWelcome to your new page", true, newProjectPage.getParentId(), false);
        return slug;
    }

    @Transactional
    public void saveProjectPage(long projectId, long pageId, String newContents) {
        if (newContents.length() > config.pages.getMaxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        ProjectPageTable pageTable = projectPagesDAO.getProjectPage(projectId, pageId);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No page found");
        }
        String oldContent = pageTable.getContents();
        pageTable.setContents(newContents);
        projectPagesDAO.update(pageTable);
        actionLogger.projectPage(LogAction.PROJECT_PAGE_EDITED.create(PageContext.of(projectId, pageId), newContents, oldContent));
    }

    @Transactional
    public void deleteProjectPage(long projectId, long pageId) {
        ProjectPageTable pageTable = projectPagesDAO.getProjectPage(projectId, pageId);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No page found");
        }
        if (!pageTable.isDeletable()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Page is not deletable");
        }
        List<ProjectPageTable> children = projectPagesDAO.getChildPages(projectId, pageId);
        if (!children.isEmpty()) {
            for (ProjectPageTable child : children) {
                child.setParentId(pageTable.getParentId());
            }
            projectPagesDAO.updateParents(children);
        }
        // Log must come first otherwise db error
        actionLogger.projectPage(LogAction.PROJECT_PAGE_DELETED.create(PageContext.of(projectId, pageId), "", pageTable.getContents()));
        projectPagesDAO.delete(pageTable);
    }
}
