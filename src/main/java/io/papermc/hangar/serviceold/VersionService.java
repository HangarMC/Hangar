package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.ProjectVersionDao;
import io.papermc.hangar.db.daoold.VisibilityDao;
import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import io.papermc.hangar.db.modelold.ProjectVersionVisibilityChangesTable;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.modelold.viewhelpers.VersionData;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import io.papermc.hangar.serviceold.project.ChannelService;
import io.papermc.hangar.serviceold.project.ProjectService;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Service("oldVersionService")
@Deprecated(forRemoval = true)
public class VersionService extends HangarService {

    private final HangarDao<ProjectVersionDao> versionDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final ProjectService projectService;
    private final ChannelService channelService;
    private final UserService userService;
    private final VersionDependencyService versionDependencyService;

    private final HttpServletRequest request;

    @Autowired
    public VersionService(HangarDao<ProjectVersionDao> versionDao, HangarDao<ProjectDao> projectDao, HangarDao<VisibilityDao> visibilityDao, ProjectService projectService, ChannelService channelService, UserService userService, VersionDependencyService versionDependencyService, HttpServletRequest request) {
        this.versionDao = versionDao;
        this.projectDao = projectDao;
        this.visibilityDao = visibilityDao;
        this.projectService = projectService;
        this.channelService = channelService;
        this.userService = userService;
        this.versionDependencyService = versionDependencyService;
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

    @Bean
    @RequestScope
    @Autowired
    public Supplier<VersionData> versionData(Supplier<ProjectData> projectDataSupplier) {
        //noinspection SpringConfigurationProxyMethods
        return () -> this.getVersionData(projectDataSupplier.get(), projectVersionsTable().get());
    }


    public ProjectVersionsTable getVersion(long projectId, long versionId) {
        return null;
//        return projectVersionVisibilityService.checkVisibility(versionDao.get().getProjectVersion(projectId, "", versionId));
    }

    public ProjectVersionsTable getVersion(String author, String slug, long versionId) {
        ProjectsTable projectsTable = projectDao.get().getBySlug(author, slug);
        return getVersion(projectsTable.getId(), versionId);
    }

    public void deleteVersion(long versionId) {
        versionDao.get().deleteVersion(versionId);
    }

    public void changeVisibility(VersionData versionData, Visibility visibility, String comment, long userId) {
        if (versionData.getV().getVisibility() == visibility) return; // No change

        visibilityDao.get().updateLatestVersionChange(userId, versionData.getV().getId());
        visibilityDao.get().insert(new ProjectVersionVisibilityChangesTable(userId, versionData.getV().getId(), comment, visibility));

        versionData.getV().setVisibility(visibility);
        versionDao.get().update(versionData.getV());
    }

    public VersionData getVersionData(ProjectData projectData, ProjectVersionsTable projectVersion) {
        ProjectChannelsTable projectChannel = channelService.getProjectChannel(projectData.getProject().getId(), projectVersion.getChannelId());
        String approvedBy = null;
        if (projectVersion.getReviewerId() != null) {
            UserData approveUser = userService.getUserData(projectVersion.getReviewerId());
            if (approveUser == null) {
                approvedBy = "Unknown";
            } else {
                approvedBy = approveUser.getUser().getName();
            }
        }

        Map<Platform, Map<PluginDependency, String>> dependencies = new EnumMap<>(Platform.class);
        versionDependencyService.getProjectVersionDependencyTables(projectVersion.getId()).forEach(pvdt -> {
            dependencies.computeIfAbsent(pvdt.getPlatform(), platform -> new HashMap<>());
            String path;
            if (pvdt.getExternalUrl() != null) {
                path = pvdt.getExternalUrl();
            } else if (pvdt.getProjectId() != null) {
                ProjectsTable projectsTable = projectService.getProjectsTable(pvdt.getProjectId());
                path = "/" + projectsTable.getOwnerName() + "/" + projectsTable.getSlug();
            } else {
                 path = null;
            }
//            dependencies.get(pvdt.getPlatform()).put(new PluginDependency(pvdt.getName(), pvdt.isRequired(), pvdt.getProjectId(), pvdt.getExternalUrl()), path);
        });
        if (true) throw new NotImplementedException();
        return new VersionData(
                projectData,
                projectVersion,
                projectChannel,
                approvedBy,
                dependencies, null
                /*versionDependencyService.getProjectVersionPlatformDependencies(projectVersion.getId())*/);
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
