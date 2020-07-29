package me.minidigger.hangar.service.project;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectPageDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
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

    public ProjectPage createPage(String contents, String name, String slug, @Nullable Long parentId, long projectId) {
        if (parentId != null) {
            Map<Long, ProjectPage> rootPages = projectPageDao.get().getRootPages(projectId);
            if (!rootPages.containsKey(parentId)) { // This prevents more than 1 level of nesting
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        if (name.equals(hangarConfig.pages.home.getName()) && contents.length() < hangarConfig.pages.getMinLen()) {
            throw new HangarException("error.minLength");
        }

        ProjectPagesTable table = new ProjectPagesTable(
                -1,
                OffsetDateTime.now(),
                projectId,
                name,
                StringUtils.slugify(slug),
                contents,
                true,
                parentId
        );
        table = projectPageDao.get().insert(table);
        return ProjectPage.of(table);
    }
}
