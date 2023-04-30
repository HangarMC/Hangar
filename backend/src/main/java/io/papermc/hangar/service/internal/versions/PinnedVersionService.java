package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.PinnedProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.model.db.versions.PinnedProjectVersionTable;
import io.papermc.hangar.model.internal.projects.HangarProject;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PinnedVersionService extends HangarComponent {

    private final HangarVersionsDAO hangarVersionsDAO;
    private final PinnedProjectVersionsDAO pinnedProjectVersionsDAO;
    private final VersionDependencyService versionDependencyService;

    @Autowired
    public PinnedVersionService(final HangarVersionsDAO hangarVersionsDAO, final PinnedProjectVersionsDAO pinnedProjectVersionsDAO, @Lazy final VersionDependencyService versionDependencyService) {
        this.hangarVersionsDAO = hangarVersionsDAO;
        this.pinnedProjectVersionsDAO = pinnedProjectVersionsDAO;
        this.versionDependencyService = versionDependencyService;
    }

    public void addPinnedVersion(final long projectId, final long versionId) {
        // TODO check if already pinned via channel
        this.pinnedProjectVersionsDAO.insert(new PinnedProjectVersionTable(projectId, versionId));
    }

    public void deletePinnedVersion(final long projectId, final long versionId) {
        this.pinnedProjectVersionsDAO.deleteVersion(projectId, versionId);
    }

    @Transactional(readOnly = true)
    public List<HangarProject.PinnedVersion> getPinnedVersions(final String user, final String project, final long projectId) {
        final List<HangarProject.PinnedVersion> versions = this.hangarVersionsDAO.getPinnedVersions(projectId);
        //TODO This is dumb and needs to be redone into as little queries as possible
        versions.parallelStream().forEach(version -> this.versionDependencyService.addDownloadsAndDependencies(user, project, version.getName(), version.getVersionId()).applyTo(version));
        return versions;
    }
}
