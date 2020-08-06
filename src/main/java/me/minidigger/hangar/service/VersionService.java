package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.VisibilityDao;
import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.db.model.ProjectVersionVisibilityChangesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.model.TagColor;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.Dependency;
import me.minidigger.hangar.model.generated.ReviewState;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ReviewQueueEntry;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.model.viewhelpers.VersionData;
import me.minidigger.hangar.service.pluginupload.PendingVersion;
import me.minidigger.hangar.service.project.ChannelService;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.HangarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectVersionDao;
import me.minidigger.hangar.db.model.ProjectVersionsTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VersionService {

    private final HangarDao<ProjectVersionDao> versionDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<VisibilityDao> visibilityDao;
    private final ProjectService projectService;
    private final ChannelService channelService;
    private final UserService userService;

    @Autowired
    public VersionService(HangarDao<ProjectVersionDao> versionDao, HangarDao<ProjectDao> projectDao, HangarDao<VisibilityDao> visibilityDao, ProjectService projectService, ChannelService channelService, UserService userService) {
        this.versionDao = versionDao;
        this.projectDao = projectDao;
        this.visibilityDao = visibilityDao;
        this.projectService = projectService;
        this.channelService = channelService;
        this.userService = userService;
    }

    public ProjectVersionsTable getVersion(long projectId, String versionString) {
        return versionDao.get().getProjectVersion(projectId,"", versionString);
    }

    public ProjectVersionsTable getVersion(String author, String slug, String versionString) {
        ProjectsTable projectsTable = projectDao.get().getBySlug(author, slug);
        return versionDao.get().getProjectVersion(projectsTable.getId(), null, versionString);
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

    public VersionData getVersionData(String author, String slug, String versionString) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ProjectVersionsTable projectVersion = getVersion(projectData.getProject().getId(), versionString);
        ProjectChannelsTable projectChannel = channelService.getProjectChannel(projectData.getProject().getId(), projectVersion.getChannelId());
        String approvedBy = null;
        if (projectVersion.getReviewerId() != null) {
            UserData approveUser = userService.getUserData(projectVersion.getReviewerId());
            if (approveUser == null) {
                approvedBy = "Unknown";
            }
            else {
                approvedBy = approveUser.getUser().getName();
            }
        }

        Map<Dependency, ProjectsTable> dependencies = Dependency.from(projectVersion.getDependencies()).stream().collect(HashMap::new, (m, v) -> {
            ProjectsTable project = projectDao.get().getByPluginId(v.getPluginId());
            m.put(v, project);
        }, HashMap::putAll);
        return new VersionData(
                projectData,
                projectVersion,
                projectChannel,
                approvedBy,
                dependencies
        );
    }
}
