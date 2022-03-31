package io.papermc.hangar.service.project;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectPageDao;
import io.papermc.hangar.db.model.ProjectPagesTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.viewhelpers.ProjectPage;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class PagesFactory {

    private final HangarDao<ProjectPageDao> projectPageDao;
    private final HangarConfig hangarConfig;

    @Autowired
    public PagesFactory(HangarDao<ProjectPageDao> projectPageDao, HangarConfig hangarConfig) {
        this.projectPageDao = projectPageDao;
        this.hangarConfig = hangarConfig;
    }

    public ProjectPagesTable createPage(String contents, String name, String slug, @Nullable Long parentId, long projectId) {
        if (parentId != null) {
            Map<Long, ProjectPage> rootPages = projectPageDao.get().getRootPages(projectId);
            if (!rootPages.containsKey(parentId)) { // This prevents more than 1 level of nesting
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        if (name.equals(hangarConfig.pages.home.getName()) && contents.length() < hangarConfig.pages.getMinLen()) {
            throw new HangarException("error.minLength");
        }

        if (!hangarConfig.projects.getNameMatcher().test(name)) {
            throw new HangarException("error.page.invalidName");
        }

        ProjectPagesTable table = new ProjectPage(
                projectId,
                name,
                StringUtils.slugify(slug),
                contents,
                true,
                parentId
        );
        table = projectPageDao.get().insert(table);
        return table;
    }

    public void update(ProjectPagesTable projectPagesTable) {
        projectPageDao.get().update(projectPagesTable);
    }
}
