package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.PinnedProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.model.db.versions.PinnedProjectVersionTable;
import io.papermc.hangar.model.internal.projects.HangarProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PinnedVersionService extends HangarComponent {

    private final HangarVersionsDAO hangarVersionsDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final PinnedProjectVersionsDAO pinnedProjectVersionsDAO;

    @Autowired
    public PinnedVersionService(final HangarVersionsDAO hangarVersionsDAO, final VersionsApiDAO versionsApiDAO, final PinnedProjectVersionsDAO pinnedProjectVersionsDAO) {
        this.hangarVersionsDAO = hangarVersionsDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.pinnedProjectVersionsDAO = pinnedProjectVersionsDAO;
    }

    public void addPinnedVersion(final long projectId, final long versionId) {
        // TODO check if already pinned via channel
        this.pinnedProjectVersionsDAO.insert(new PinnedProjectVersionTable(projectId, versionId));
    }

    public void deletePinnedVersion(final long projectId, final long versionId) {
        this.pinnedProjectVersionsDAO.deleteVersion(projectId, versionId);
    }

    public List<HangarProject.PinnedVersion> getPinnedVersions(final long projectId) {
        final List<HangarProject.PinnedVersion> versions = this.hangarVersionsDAO.getPinnedVersions(projectId);
        for (final HangarProject.PinnedVersion version : versions) {
            version.getPlatformDependencies().putAll(versionsApiDAO.getPlatformDependencies(version.getVersionId()));
        }
        return versions;
    }
}
