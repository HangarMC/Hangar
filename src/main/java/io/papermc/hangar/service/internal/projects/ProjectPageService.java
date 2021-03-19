package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarProjectPagesDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectPagesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectHomePageTable;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.model.internal.HangarProjectPage;
import io.papermc.hangar.model.internal.HangarViewProjectPage;
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
    private final HangarProjectPagesDAO hangarProjectPagesDAO;

    public ProjectPageService(HangarDao<ProjectPagesDAO> projectPagesDAO, HangarDao<HangarProjectPagesDAO> hangarProjectPagesDAO) {
        this.projectPagesDAO = projectPagesDAO.get();
        this.hangarProjectPagesDAO = hangarProjectPagesDAO.get();
    }

    public ProjectPageTable createPage(long projectId, String name, String slug, String contents, boolean deletable, @Nullable Long parentId, boolean isHome) {
        if (!isHome && contents.length() < config.pages.getMinLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.minLength");
        }

//        if ((!isHome && name.equalsIgnoreCase(hangarConfig.pages.home.getName())) && contents.length() < hangarConfig.pages.getMinLen()) {
//            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.minLength");
//        }
        if (contents.length() >  config.pages.getMaxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        config.pages.testPageName(name);

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
        if (isHome) {
            projectPagesDAO.insertHomePage(new ProjectHomePageTable(projectPageTable.getProjectId(), projectPageTable.getId()));
        }
        userActionLogService.projectPage(LoggedActionType.PROJECT_PAGE_CREATED.with(ProjectPageContext.of(projectPageTable.getProjectId(), projectPageTable.getId())), contents, "");
        return projectPageTable;
    }

    public Map<Long, HangarProjectPage> getProjectPages(long projectId) {
        Map<Long, HangarProjectPage> hangarProjectPages = new LinkedHashMap<>();
        for (HangarViewProjectPage projectPage : hangarProjectPagesDAO.getProjectPages(projectId)) {
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

    public HangarViewProjectPage getProjectPage(String author, String slug, String requestUri) {
        String[] path = requestUri.split("/", 8);
        HangarViewProjectPage pageTable;
        if (path.length < 8) {
            pageTable = hangarProjectPagesDAO.getHomePage(author, slug);
        } else {
            pageTable = hangarProjectPagesDAO.getProjectPage(author, slug, path[7]);
        }
        if (pageTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Page not found");
        }
        return pageTable;
    }

    public String createProjectPage(long projectId, NewProjectPage newProjectPage) {
        String slug = StringUtils.slugify(newProjectPage.getName());
        if (newProjectPage.getParentId() != null) {
            slug = projectPagesDAO.getProjectPage(projectId, newProjectPage.getParentId()).getSlug() + "/" + slug;
        }
        createPage(projectId, newProjectPage.getName(), slug, "# " + newProjectPage.getName() + "\n\nWelcome to your new page", true, newProjectPage.getParentId(), false);
        return slug;
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
