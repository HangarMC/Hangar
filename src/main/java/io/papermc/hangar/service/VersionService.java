package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.ProjectVersionDao;
import io.papermc.hangar.db.dao.VisibilityDao;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionVisibilityChangesTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.TagColor;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ReviewQueueEntry;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.service.pluginupload.PendingVersion;
import io.papermc.hangar.service.project.ChannelService;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.RequestUtil;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class VersionService extends HangarService {

    private final HangarDao<ProjectVersionDao> versionDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final ProjectService projectService;
    private final ChannelService channelService;
    private final UserService userService;
    private final PermissionService permissionService;

    private final HttpServletRequest request;

    @Autowired
    public VersionService(HangarDao<ProjectVersionDao> versionDao, HangarDao<ProjectDao> projectDao, HangarDao<VisibilityDao> visibilityDao, ProjectService projectService, ChannelService channelService, UserService userService, PermissionService permissionService, HttpServletRequest request) {
        this.versionDao = versionDao;
        this.projectDao = projectDao;
        this.visibilityDao = visibilityDao;
        this.projectService = projectService;
        this.channelService = channelService;
        this.userService = userService;
        this.permissionService = permissionService;
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
    public Supplier<VersionData> versionData() {
        //noinspection SpringConfigurationProxyMethods
        return () -> this.getVersionData(projectService.projectData().get(), projectVersionsTable().get());
    }

    public ProjectVersionsTable getMostRelevantVersion(ProjectsTable project) {
        Optional<ProjectVersionsTable> version = Optional.ofNullable(getRecommendedVersion(project));
        return version.or(() -> Optional.ofNullable(getMostRecentVersion(project))).orElse(null);
    }

    public ProjectVersionsTable getMostRecentVersion(ProjectsTable project) {
        return versionDao.get().getMostRecentVersion(project.getId());
    }

    public ProjectVersionsTable getRecommendedVersion(ProjectsTable project) {
        if (project.getRecommendedVersionId() == null) {
            return null;
        }
        return versionDao.get().getProjectVersion(project.getId(), "", project.getRecommendedVersionId());
    }

    public ProjectVersionsTable getVersion(long projectId, long versionId) {
        Permission perms = permissionService.getProjectPermissions(currentUser.get().map(UsersTable::getId).orElse(-10L), projectId);
        ProjectVersionsTable pvt = versionDao.get().getProjectVersion(projectId, "", versionId);
        if (!perms.has(Permission.SeeHidden) && !perms.has(Permission.IsProjectMember) && pvt.getVisibility() != Visibility.PUBLIC) {
            return null;
        }
        return pvt;
    }

    public ProjectVersionsTable getVersion(String author, String slug, long versionId) {
        ProjectsTable projectsTable = projectDao.get().getBySlug(author, slug);
        return getVersion(projectsTable.getId(), versionId);
    }

    public void update(ProjectVersionsTable projectVersionsTable) {
        versionDao.get().update(projectVersionsTable);
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

    public List<ReviewQueueEntry> getReviewQueue() {
        return versionDao.get().getQueue(ReviewState.UNREVIEWED);
    }

    public boolean exists(PendingVersion pendingVersion) {
        ProjectsTable project = projectDao.get().getById(pendingVersion.getProjectId());
        if (project == null) {
            throw new HangarException("error.project.notFound", String.valueOf(pendingVersion.getProjectId()));
        }
        ProjectVersionsTable version = versionDao.get().getProjectVersion(pendingVersion.getProjectId(), pendingVersion.getHash(), pendingVersion.getVersionString());
        return version != null;
    }

    public List<ProjectVersionTagsTable> insertTags(List<ProjectVersionTagsTable> tags) {
        return versionDao.get().insertTags(tags);
    }

    public void addUnstableTag(long versionId) {
        versionDao.get().insertTag(new ProjectVersionTagsTable(
                -1,
                versionId,
                "Unstable",
                null,
                TagColor.UNSTABLE
        ));
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

        Map<Platform, Map<Dependency, String>> dependencies = new EnumMap<>(Platform.class);
        projectVersion.getDependencies().forEach((platform, deps) -> dependencies.put(
                platform,
                deps.stream().collect(
                        HashMap::new,
                        (m, d) -> {
                            if (d.getExternalUrl() != null) {
                                m.put(d, d.getExternalUrl());
                            } else if (d.getProjectId() != null) {
                                ProjectsTable projectsTable = projectService.getProjectsTable(d.getProjectId());
                                m.put(d, "/" + projectsTable.getOwnerName() + "/" + projectsTable.getSlug());
                            } else {
                                m.put(d, null);
                            }},
                        HashMap::putAll
                )
                ));
        return new VersionData(
                projectData,
                projectVersion,
                projectChannel,
                approvedBy,
                dependencies
        );
    }

    public Map<ProjectVersionVisibilityChangesTable, String> getVersionVisibilityChanges(long versionId) {
        return visibilityDao.get().getProjectVersionVisibilityChanges(versionId);
    }
}
