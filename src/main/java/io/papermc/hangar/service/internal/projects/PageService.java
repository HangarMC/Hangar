package io.papermc.hangar.service.internal.projects;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectPagesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import io.papermc.hangar.service.HangarService;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PageService extends HangarService {

    private final ProjectPagesDAO projectPagesDAO;

    public PageService(HangarDao<ProjectPagesDAO> projectPagesDAO) {
        this.projectPagesDAO = projectPagesDAO.get();
    }

    public ProjectPageTable createPage(long projectId, String name, String slug, String contents, boolean deletable, @Nullable Long parentId, boolean isHome) {
        if (parentId != null) {
            if (projectPagesDAO.getRootPages(projectId).containsKey(parentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        if ((!isHome && name.equalsIgnoreCase(hangarConfig.pages.home.getName())) && contents.length() < hangarConfig.pages.getMinLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.page.minLength");
        }

        if (!hangarConfig.projects.getNameMatcher().test(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "error.page.invalidName");
        }

        ProjectPageTable projectPageTable = new ProjectPageTable(
                projectId,
                name,
                slug,
                contents,
                deletable,
                parentId
        );
        return projectPagesDAO.insert(projectPageTable);
    }
}
