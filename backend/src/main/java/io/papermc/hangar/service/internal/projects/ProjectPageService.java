package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectPagesDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectPagesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectHomePageTable;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectPage;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.PageContext;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectPageService extends HangarComponent {

    private final ProjectPagesDAO projectPagesDAO;
    private final HangarProjectPagesDAO hangarProjectPagesDAO;
    private final ValidationService validationService;

    public ProjectPageService(final ProjectPagesDAO projectPagesDAO, final HangarProjectPagesDAO hangarProjectPagesDAO, final ValidationService validationService) {
        this.projectPagesDAO = projectPagesDAO;
        this.hangarProjectPagesDAO = hangarProjectPagesDAO;
        this.validationService = validationService;
    }

    public void checkDuplicateName(final long projectId, final String name, final @Nullable Long parentId) {
        if ((parentId != null && this.projectPagesDAO.getChildPage(projectId, parentId, name) != null) || (parentId == null && this.projectPagesDAO.getRootPage(projectId, StringUtils.slugify(name)) != null)) {
            throw new HangarApiException("page.new.error.duplicateName");
        }
    }

    @Transactional
    public ProjectPageTable createPage(final long projectId, final String name, final String slug, final String contents, final boolean deletable, final @Nullable Long parentId, final boolean isHome) {
        if (!isHome && contents.length() < this.config.pages.minLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.minLength");
        }
        if (contents.length() > this.config.pages.maxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }
        if (slug.length() > this.config.pages.maxSlugLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxSlugLength");
        }
        if (org.apache.commons.lang3.StringUtils.countMatches(slug, '/') > this.config.pages.maxNestingLevel()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxSlugLength");
        }

        this.validationService.testPageName(name);

        this.checkDuplicateName(projectId, name, parentId);

        ProjectPageTable projectPageTable = new ProjectPageTable(
            projectId,
            name,
            slug,
            contents,
            deletable,
            parentId
        );
        projectPageTable = this.projectPagesDAO.insert(projectPageTable);
        if (isHome) {
            this.projectPagesDAO.insertHomePage(new ProjectHomePageTable(projectPageTable.getProjectId(), projectPageTable.getId()));
        }
        this.actionLogger.projectPage(LogAction.PROJECT_PAGE_CREATED.create(PageContext.of(projectPageTable.getProjectId(), projectPageTable.getId()), contents, ""));
        return projectPageTable;
    }

    public Map<Long, HangarProjectPage> getProjectPages(final long projectId) {
        final Map<Long, HangarProjectPage> hangarProjectPages = new LinkedHashMap<>();
        for (final ExtendedProjectPage projectPage : this.hangarProjectPagesDAO.getProjectPages(projectId)) {
            if (projectPage.getParentId() == null) {
                hangarProjectPages.put(projectPage.getId(), new HangarProjectPage(projectPage, projectPage.isHome()));
            } else {
                final HangarProjectPage parent = this.findById(projectPage.getParentId(), hangarProjectPages);
                if (parent == null) {
                    throw new IllegalStateException("Should always find a parent");
                }
                parent.getChildren().put(projectPage.getId(), new HangarProjectPage(projectPage, projectPage.isHome()));
            }
        }

        return hangarProjectPages;
    }

    private HangarProjectPage findById(final long id, final Map<Long, HangarProjectPage> pageMap) {
        if (pageMap.containsKey(id)) {
            return pageMap.get(id);
        }

        for (final HangarProjectPage page : pageMap.values()) {
            final HangarProjectPage possiblePage = this.findById(id, page.getChildren());
            if (possiblePage != null) {
                return possiblePage;
            }
        }
        return null;
    }

    public ExtendedProjectPage getProjectPageFromURI(final String slug, final String uri) {
        final String path = uri.replace("/api/internal/pages/page/" + slug, "");
        return this.getProjectPage(slug, path);
    }

    public ExtendedProjectPage getProjectPage(final String slug, String path) {
        final ExtendedProjectPage pageTable;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        pageTable = path.isEmpty() ? this.hangarProjectPagesDAO.getHomePage(slug) : this.hangarProjectPagesDAO.getProjectPage(slug, path);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Page not found");
        }
        return pageTable;
    }

    public ExtendedProjectPage getProjectPage(final long id) {
        return this.hangarProjectPagesDAO.getProjectPage(id);
    }

    public ExtendedProjectPage getProjectHomePage(final long projectId) {
        return this.hangarProjectPagesDAO.getProjectHomePage(projectId);
    }

    @Transactional
    public String createProjectPage(final long projectId, final NewProjectPage newProjectPage) {
        String slug = StringUtils.slugify(newProjectPage.getName());
        if (newProjectPage.getParentId() != null) {
            slug = this.projectPagesDAO.getProjectPage(projectId, newProjectPage.getParentId()).getSlug() + "/" + slug;
        }
        this.createPage(projectId, newProjectPage.getName(), slug, "# " + newProjectPage.getName() + "\n\nWelcome to your new page", true, newProjectPage.getParentId(), false);
        return slug;
    }

    @Transactional
    public void saveProjectPage(final long projectId, final long pageId, final String newContents) {
        if (newContents.length() > this.config.pages.maxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        final ProjectPageTable pageTable = this.projectPagesDAO.getProjectPage(projectId, pageId);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No page found");
        }
        final String oldContent = pageTable.getContents();
        pageTable.setContents(newContents);
        this.projectPagesDAO.update(pageTable);
        this.actionLogger.projectPage(LogAction.PROJECT_PAGE_EDITED.create(PageContext.of(projectId, pageId), newContents, oldContent));
    }

    @Transactional
    public void deleteProjectPage(final long projectId, final long pageId) {
        final ProjectPageTable pageTable = this.projectPagesDAO.getProjectPage(projectId, pageId);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No page found");
        }
        if (!pageTable.isDeletable()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Page is not deletable");
        }
        final List<ProjectPageTable> children = this.projectPagesDAO.getChildPages(projectId, pageId);
        if (!children.isEmpty()) {
            for (final ProjectPageTable child : children) {
                child.setParentId(pageTable.getParentId());
            }
            this.projectPagesDAO.updateParents(children);
        }
        // Log must come first otherwise db error
        this.actionLogger.projectPage(LogAction.PROJECT_PAGE_DELETED.create(PageContext.of(projectId, pageId), "", pageTable.getContents()));
        this.projectPagesDAO.delete(pageTable);
    }
}
