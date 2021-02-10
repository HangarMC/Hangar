package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectPagesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.model.internal.HangarProjectPage;
import io.papermc.hangar.model.internal.api.requests.projects.NewProjectPage;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectPageService extends HangarService {

    private final ProjectPagesDAO projectPagesDAO;

    public ProjectPageService(HangarDao<ProjectPagesDAO> projectPagesDAO) {
        this.projectPagesDAO = projectPagesDAO.get();
    }

    public ProjectPageTable createPage(long projectId, String name, String slug, String contents, boolean deletable, @Nullable Long parentId, boolean isHome) {

        if ((!isHome && name.equalsIgnoreCase(hangarConfig.pages.home.getName())) && contents.length() < hangarConfig.pages.getMinLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.minLength");
        }

        if (contents.length() >  hangarConfig.pages.getMaxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        hangarConfig.pages.testPageName(name);

        if (parentId != null && projectPagesDAO.getChildPage(projectId, parentId, slug) != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.duplicateName");
        }

        ProjectPageTable projectPageTable = new ProjectPageTable(
                projectId,
                name,
                slug,
                contents,
                deletable,
                parentId
        );
        projectPageTable = projectPagesDAO.insert(projectPageTable);
        userActionLogService.projectPage(LoggedActionType.PROJECT_PAGE_CREATED.with(ProjectPageContext.of(projectPageTable.getProjectId(), projectPageTable.getId())), contents, "");
        return projectPageTable;
    }

    public Map<Long, HangarProjectPage> getProjectPages(long projectId) {
        Map<Long, HangarProjectPage> hangarProjectPages = new LinkedHashMap<>();
        for (ProjectPageTable projectPage : projectPagesDAO.getProjectPages(projectId)) {
            if (projectPage.getParentId() == null) {
                hangarProjectPages.put(projectPage.getId(), new HangarProjectPage(projectPage, projectPage.getName().equals(hangarConfig.pages.home.getName())));
            } else {
                HangarProjectPage parent = findById(projectPage.getParentId(), hangarProjectPages);
                if (parent == null) {
                    throw new IllegalStateException("Should always find a parent");
                }
                parent.getChildren().put(projectPage.getId(), new HangarProjectPage(projectPage, projectPage.getName().equals(hangarConfig.pages.home.getName())));
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

    public ProjectPageTable getProjectPage(String author, String slug, String pageSlug) {
        ProjectPageTable pageTable = projectPagesDAO.getProjectPage(author, slug, pageSlug);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Page not found");
        }
        return pageTable;
    }

    public String createProjectPage(long projectId, NewProjectPage newProjectPage) {
        String slug = createSlug(projectId, StringUtils.slugify(newProjectPage.getName()), newProjectPage.getParentId());
        createPage(projectId, newProjectPage.getName(), slug, "# " + newProjectPage.getName() + "\n\nWelcome to your new page", true, newProjectPage.getParentId(), false);
        return slug;
    }

    private String createSlug(long projectId, String slug, @Nullable Long parentId) {
        if (parentId == null) {
            return slug;
        } else {
            ProjectPageTable pageTable = projectPagesDAO.getProjectPage(projectId, parentId);
            return createSlug(projectId, pageTable.getSlug() + "/" + slug, pageTable.getParentId());
        }
    }

    public void saveProjectPage(long projectId, long pageId, String newContents) {
        ProjectPageTable pageTable = projectPagesDAO.getProjectPage(projectId, pageId);
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No page found");
        }
        String oldContent = pageTable.getContents();
        pageTable.setContents(newContents);
        projectPagesDAO.update(pageTable);
        userActionLogService.projectPage(LoggedActionType.PROJECT_PAGE_EDITED.with(ProjectPageContext.of(projectId, pageId)), newContents, oldContent);
    }

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
        userActionLogService.projectPage(LoggedActionType.PROJECT_PAGE_DELETED.with(ProjectPageContext.of(projectId, pageId)), "", pageTable.getContents());
        projectPagesDAO.delete(pageTable);
    }
}
