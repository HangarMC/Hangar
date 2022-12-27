package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import io.papermc.hangar.service.internal.visibility.ProjectVisibilityService;
import io.papermc.hangar.util.StringUtils;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionService extends HangarComponent {

    private final ProjectVersionsDAO projectVersionsDAO;
    private final HangarVersionsDAO hangarVersionsDAO;
    private final ProjectVisibilityService projectVisibilityService;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final VersionDependencyService versionDependencyService;
    private final ProjectFiles projectFiles;
    private final ProjectsDAO projectsDAO;
    private final FileService fileService;

    @Autowired
    public VersionService(final ProjectVersionsDAO projectVersionDAO, final HangarVersionsDAO hangarProjectsDAO, final ProjectVisibilityService projectVisibilityService, final ProjectVersionVisibilityService projectVersionVisibilityService, final VersionDependencyService versionDependencyService, final ProjectFiles projectFiles, final ProjectsDAO projectsDAO, final FileService fileService) {
        this.projectVersionsDAO = projectVersionDAO;
        this.hangarVersionsDAO = hangarProjectsDAO;
        this.projectVisibilityService = projectVisibilityService;
        this.projectVersionVisibilityService = projectVersionVisibilityService;
        this.versionDependencyService = versionDependencyService;
        this.projectFiles = projectFiles;
        this.projectsDAO = projectsDAO;
        this.fileService = fileService;
    }

    public @Nullable ProjectVersionTable getProjectVersionTable(final Long versionId) {
        if (versionId == null) {
            return null;
        }
        return this.projectVersionVisibilityService.checkVisibility(this.projectVersionsDAO.getProjectVersionTable(versionId));
    }

    public @Nullable ProjectVersionTable getProjectVersionTable(final String author, final String slug, final String versionString) {
        return this.projectVersionVisibilityService.checkVisibility(this.projectVersionsDAO.getProjectVersionTable(author, slug, versionString));
    }

    public void updateProjectVersionTable(final ProjectVersionTable projectVersionTable) {
        this.projectVersionsDAO.update(projectVersionTable);
    }

    public HangarVersion getHangarVersion(final String author, final String slug, final String versionString) {
        final HangarVersion version = this.hangarVersionsDAO.getVersionWithVersionString(author, slug, versionString, this.getGlobalPermissions().has(Permission.SeeHidden), this.getHangarUserId());
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        this.versionDependencyService.addDownloadsAndDependencies(author, slug, versionString, version.getId(), version);
        return version;
    }

    @Transactional
    public void softDeleteVersion(final long projectId, final ProjectVersionTable pvt, final String comment) {
        if (pvt.getVisibility() == Visibility.SOFTDELETE) {
            return;
        }

        final List<ProjectVersionTable> projectVersionTables = this.projectVersionsDAO.getProjectVersions(projectId);
        if (projectVersionTables.stream().filter(pv -> pv.getVisibility() == Visibility.PUBLIC).count() <= 1 && pvt.getVisibility() == Visibility.PUBLIC) {
            this.projectVisibilityService.changeVisibility(projectId, Visibility.NEW, "Visibility reset to new because no public version exists");
        }

        // Append deletion suffix to allow creation of a new version under the same name
        int deletedId = -1;
        for (int i = 0; i < 10; i++) {
            if (this.projectVersionsDAO.getProjectVersion(pvt.getProjectId(), pvt.getVersionString() + ProjectFactory.SOFT_DELETION_SUFFIX + i) == null) {
                deletedId = i;
                break;
            }
        }
        if (deletedId == -1) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Version has been deleted too often");
        }

        final Visibility oldVisibility = pvt.getVisibility();
        this.projectVersionVisibilityService.changeVisibility(pvt, Visibility.SOFTDELETE, comment);
        this.actionLogger.version(LogAction.VERSION_DELETED.create(VersionContext.of(projectId, pvt.getId()), "Soft Delete: " + comment, oldVisibility.getTitle()));
        this.renameVersion(pvt, pvt.getVersionString() + ProjectFactory.SOFT_DELETION_SUFFIX + deletedId);
    }

    private void renameVersion(final ProjectVersionTable projectVersionTable, final String newName) {
        final String compactNewName = StringUtils.compact(newName);
        final String oldVersion = projectVersionTable.getVersionString();
        projectVersionTable.setVersionString(compactNewName);
        this.projectVersionsDAO.update(projectVersionTable);

        final ProjectTable project = this.projectsDAO.getById(projectVersionTable.getProjectId());
        this.projectFiles.renameVersion(project.getOwnerName(), project.getSlug(), oldVersion, compactNewName);
    }

    @Transactional
    public void hardDeleteVersion(final ProjectTable pt, final ProjectVersionTable pvt, final String comment) {
        final List<ProjectVersionTable> projectVersionTables = this.projectVersionsDAO.getProjectVersions(pt.getId());
        final boolean hasOtherPublicVersion = projectVersionTables.stream().filter(pv -> pv.getId() != pvt.getId()).anyMatch(pv -> pv.getVisibility() == Visibility.PUBLIC);
        if (!hasOtherPublicVersion && pt.getVisibility() == Visibility.PUBLIC) {
            this.projectVisibilityService.changeVisibility(pt, Visibility.NEW, "Visibility reset to new because no public version exists");
        }

        this.actionLogger.version(LogAction.VERSION_DELETED.create(VersionContext.of(pt.getId(), pvt.getId()), "Deleted: " + comment, pvt.getVisibility().getTitle()));
        this.projectVersionsDAO.delete(pvt);
        this.fileService.deleteDirectory(this.projectFiles.getVersionDir(pt.getOwnerName(), pt.getSlug(), pvt.getVersionString()));
    }

    @Transactional
    public void restoreVersion(final long projectId, final ProjectVersionTable pvt) {
        if (pvt.getVisibility() != Visibility.SOFTDELETE) {
            return;
        }

        final int suffixIndex = pvt.getVersionString().indexOf(ProjectFactory.SOFT_DELETION_SUFFIX);
        if (suffixIndex != -1) {
            final String newName = pvt.getVersionString().substring(0, suffixIndex);
            if (this.projectVersionsDAO.getProjectVersion(pvt.getProjectId(), newName) != null) {
                // Can't automatically rename
                throw new HangarApiException("version.error.oldNameTaken");
            }

            this.renameVersion(pvt, newName);
        }
        this.projectVersionVisibilityService.changeVisibility(pvt, Visibility.PUBLIC, "Version Restored");
        this.actionLogger.version(LogAction.VERSION_DELETED.create(VersionContext.of(projectId, pvt.getId()), "Version Restored", Visibility.SOFTDELETE.getTitle()));
    }

    public void saveDiscourseData(final ProjectVersionTable version, final long postId) {
        version.setPostId(postId);
        this.projectVersionsDAO.update(version);
    }
}
