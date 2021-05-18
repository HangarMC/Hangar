package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VersionService extends HangarComponent {

    private final ProjectVersionsDAO projectVersionsDAO;
    private final HangarVersionsDAO hangarVersionsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final VersionDependencyService versionDependencyService;
    private final ProjectFiles projectFiles;

    @Autowired
    public VersionService(HangarDao<ProjectVersionsDAO> projectVersionDAO, HangarDao<HangarVersionsDAO> hangarProjectsDAO, ProjectVisibilityService projectVisibilityService, ProjectVersionVisibilityService projectVersionVisibilityService, VersionDependencyService versionDependencyService, ProjectFiles projectFiles) {
        this.projectVersionsDAO = projectVersionDAO.get();
        this.hangarVersionsDAO = hangarProjectsDAO.get();
        this.projectVisibilityService = projectVisibilityService;
        this.projectVersionVisibilityService = projectVersionVisibilityService;
        this.versionDependencyService = versionDependencyService;
        this.projectFiles = projectFiles;
    }

    @Nullable
    public ProjectVersionTable getProjectVersionTable(Long versionId) {
        if (versionId == null) {
            return null;
        }
        return projectVersionVisibilityService.checkVisibility(projectVersionsDAO.getProjectVersionTable(versionId));
    }

    @Nullable
    public ProjectVersionTable getProjectVersionTable(String author, String slug, String versionString, Platform platform) {
        return projectVersionVisibilityService.checkVisibility(projectVersionsDAO.getProjectVersionTable(author, slug, versionString, platform));
    }

    public void updateProjectVersionTable(ProjectVersionTable projectVersionTable) {
        projectVersionsDAO.update(projectVersionTable);
    }

    public HangarVersion getHangarVersion(String author, String slug, String versionString, Platform platform) {
        ProjectVersionTable projectVersionTable = getProjectVersionTable(author, slug, versionString, platform);
        if (projectVersionTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        HangarVersion hangarVersion = hangarVersionsDAO.getVersion(projectVersionTable.getId(), getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        return versionDependencyService.addDependenciesAndTags(hangarVersion.getId(), hangarVersion);
    }

    public List<HangarVersion> getHangarVersions(String author, String slug, String versionString) {
        List<HangarVersion> versions = hangarVersionsDAO.getVersionsWithVersionString(author, slug, versionString, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        if (versions.isEmpty()) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return versions.stream().map(v -> versionDependencyService.addDependenciesAndTags(v.getId(), v)).collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteVersion(long projectId, ProjectVersionTable pvt, String comment) {
        if (pvt.getVisibility() != Visibility.SOFTDELETE) {
            List<ProjectVersionTable> projectVersionTables = projectVersionsDAO.getProjectVersions(projectId);
            // TODO should this disallow? or just reset project visibility to NEW
            if (projectVersionTables.stream().filter(pv -> pv.getVisibility() == Visibility.PUBLIC).count() <= 1 && pvt.getVisibility() == Visibility.PUBLIC) {
                throw new HangarApiException("version.error.onlyOnePublic");
            }

            Visibility oldVisibility = pvt.getVisibility();
            projectVersionVisibilityService.changeVisibility(pvt, Visibility.SOFTDELETE, comment);
            actionLogger.version(LogAction.VERSION_DELETED.create(VersionContext.of(projectId, pvt.getId()), "Soft Delete: " + comment, oldVisibility.getTitle()));
        }
    }

    @Transactional
    public void hardDeleteVersion(ProjectTable pt, ProjectVersionTable pvt, String comment) {
        List<ProjectVersionTable> projectVersionTables = projectVersionsDAO.getProjectVersions(pt.getId());
        boolean hasOtherPublicVersion = projectVersionTables.stream().filter(pv -> pv.getId() != pvt.getId()).anyMatch(pv -> pv.getVisibility() == Visibility.PUBLIC);
        if (!hasOtherPublicVersion && pt.getVisibility() == Visibility.PUBLIC) {
            projectVisibilityService.changeVisibility(pt, Visibility.NEW, "Visibility reset to new because no public versions exist");
        }

        actionLogger.version(LogAction.VERSION_DELETED.create(VersionContext.of(pt.getId(), pvt.getId()), "Deleted: " + comment, pvt.getVisibility().getTitle()));
        List<Platform> versionPlatforms = projectVersionsDAO.getVersionPlatforms(pvt.getId());
        for (Platform platform : versionPlatforms) {
            FileUtils.deleteDirectory(projectFiles.getVersionDir(pt.getOwnerName(), pt.getName(), pvt.getVersionString(), platform));
        }
        projectVersionsDAO.delete(pvt);
    }

    @Transactional
    public void restoreVersion(long projectId, ProjectVersionTable pvt) {
        if (pvt.getVisibility() == Visibility.SOFTDELETE) {
            projectVersionVisibilityService.changeVisibility(pvt, Visibility.PUBLIC, "Version Restored");
            actionLogger.version(LogAction.VERSION_DELETED.create(VersionContext.of(projectId, pvt.getId()), "Version Restored", Visibility.SOFTDELETE.getTitle()));
        }
    }

    public void saveDiscourseData(ProjectVersionTable version, long postId) {
        version.setPostId(postId);
        projectVersionsDAO.update(version);
    }
}
