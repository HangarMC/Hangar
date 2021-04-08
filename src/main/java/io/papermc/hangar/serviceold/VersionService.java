package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.ProjectVersionDao;
import io.papermc.hangar.db.daoold.VisibilityDao;
import io.papermc.hangar.db.modelold.ProjectVersionVisibilityChangesTable;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Service("oldVersionService")
@Deprecated(forRemoval = true)
public class VersionService extends HangarService {

    private final HangarDao<ProjectVersionDao> versionDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<VisibilityDao> visibilityDao;

    private final HttpServletRequest request;

    @Autowired
    public VersionService(HangarDao<ProjectVersionDao> versionDao, HangarDao<ProjectDao> projectDao, HangarDao<VisibilityDao> visibilityDao, HttpServletRequest request) {
        this.versionDao = versionDao;
        this.projectDao = projectDao;
        this.visibilityDao = visibilityDao;
        this.request = request;
    }

    @Bean
    @RequestScope
    public Supplier<ProjectVersionsTable> projectVersionsTable() {
        Map<String, String> pathParams = RequestUtil.getPathParams(request);
        if (pathParams.keySet().containsAll(Set.of("author", "slug", "version"))) {
            ProjectVersionsTable pvt = this.getVersion(pathParams.get("author"), pathParams.get("slug"), StringUtils.getVersionId(pathParams.get("version"), new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badly formatted version string")));
            if (pvt == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return () -> pvt;
        } else {
            return () -> null;
        }
    }


    public ProjectVersionsTable getVersion(long projectId, long versionId) {
        return null;
//        return projectVersionVisibilityService.checkVisibility(versionDao.get().getProjectVersion(projectId, "", versionId));
    }

    public ProjectVersionsTable getVersion(String author, String slug, long versionId) {
        ProjectsTable projectsTable = projectDao.get().getBySlug(author, slug);
        return getVersion(projectsTable.getId(), versionId);
    }

    public Map<ProjectVersionVisibilityChangesTable, String> getVersionVisibilityChanges(long versionId) {
        return visibilityDao.get().getProjectVersionVisibilityChanges(versionId);
    }

    @Service
    public static class RecommendedVersionService {

        private final ProjectVersionVisibilityService visibilityService;
        private final HangarDao<ProjectVersionDao> versionDao;

        @Autowired
        public RecommendedVersionService(ProjectVersionVisibilityService visibilityService, HangarDao<ProjectVersionDao> versionDao) {
            this.visibilityService = visibilityService;
            this.versionDao = versionDao;
        }

        @Nullable
        public ProjectVersionsTable getRecommendedVersion(ProjectsTable project) {
            if (project.getRecommendedVersionId() == null) {
                return null;
            }
//            return visibilityService.checkVisibility(versionDao.get().getProjectVersion(project.getId(), "", project.getRecommendedVersionId()), ProjectVersionsTable::getProjectId);
            return null;
        }

    }
}
