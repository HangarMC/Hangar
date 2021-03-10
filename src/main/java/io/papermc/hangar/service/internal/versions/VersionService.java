package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarVersionsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.TagColor;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.VisibilityService.ProjectVersionVisibilityService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class VersionService extends HangarService {

    private final ProjectVersionsDAO projectVersionsDAO;
    private final HangarVersionsDAO hangarVersionsDAO;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;

    @Autowired
    public VersionService(HangarDao<ProjectVersionsDAO> projectVersionDAO, HangarDao<HangarVersionsDAO> hangarProjectsDAO, ProjectVersionVisibilityService projectVersionVisibilityService) {
        this.projectVersionsDAO = projectVersionDAO.get();
        this.hangarVersionsDAO = hangarProjectsDAO.get();
        this.projectVersionVisibilityService = projectVersionVisibilityService;
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
        return hangarVersionsDAO.getVersion(author, slug, versionString, platform, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId()).orElseThrow(() -> new HangarApiException(HttpStatus.NOT_FOUND));
    }

    public List<HangarVersion> getHangarVersions(String author, String slug, String versionString) {
        List<HangarVersion> versions = hangarVersionsDAO.getVersions(author, slug, versionString, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        if (versions.isEmpty()) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return versions;
    }

    public void addUnstableTag(long versionId) {
        projectVersionsDAO.insertTags(Set.of(
                new ProjectVersionTagTable(versionId, "Unstable", null, TagColor.UNSTABLE)
        ));
    }
}
