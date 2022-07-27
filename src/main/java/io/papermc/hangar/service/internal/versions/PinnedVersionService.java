package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.PinnedProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.PinnedProjectVersionTable;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PinnedVersionService extends HangarComponent {

    private final HangarVersionsDAO hangarVersionsDAO;
    private final VersionsApiDAO versionsApiDAO;
    private final PinnedProjectVersionsDAO pinnedProjectVersionsDAO;
    private final DownloadService downloadService;

    @Autowired
    public PinnedVersionService(final HangarVersionsDAO hangarVersionsDAO, final VersionsApiDAO versionsApiDAO, final PinnedProjectVersionsDAO pinnedProjectVersionsDAO, final DownloadService downloadService) {
        this.hangarVersionsDAO = hangarVersionsDAO;
        this.versionsApiDAO = versionsApiDAO;
        this.pinnedProjectVersionsDAO = pinnedProjectVersionsDAO;
        this.downloadService = downloadService;
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
            final Map<Platform, SortedSet<String>> platformDependencies = versionsApiDAO.getPlatformDependencies(version.getVersionId());
            for (final Map.Entry<Platform, SortedSet<String>> entry : platformDependencies.entrySet()) {
                version.getPlatformDependenciesFormatted().put(entry.getKey(), StringUtils.formatVersionNumbers(new ArrayList<>(entry.getValue())));
            }
            downloadService.addDownloads(version.getVersionId(), version.getDownloads());
        }
        return versions;
    }
}
