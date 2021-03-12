package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.VisibilityService.ProjectVersionVisibilityService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VersionService extends HangarService {

    private final ProjectVersionsDAO projectVersionsDAO;
    private final HangarVersionsDAO hangarVersionsDAO;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final VersionDependencyService versionDependencyService;

    @Autowired
    public VersionService(HangarDao<ProjectVersionsDAO> projectVersionDAO, HangarDao<HangarVersionsDAO> hangarProjectsDAO, ProjectVersionVisibilityService projectVersionVisibilityService, VersionDependencyService versionDependencyService) {
        this.projectVersionsDAO = projectVersionDAO.get();
        this.hangarVersionsDAO = hangarProjectsDAO.get();
        this.projectVersionVisibilityService = projectVersionVisibilityService;
        this.versionDependencyService = versionDependencyService;
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
}
