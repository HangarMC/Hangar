package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.versions.PinnedProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarVersionsDAO;
import io.papermc.hangar.model.db.versions.PinnedProjectVersionTable;
import io.papermc.hangar.model.internal.projects.HangarProject;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PinnedVersionService extends HangarComponent {

    private final HangarVersionsDAO hangarVersionsDAO;
    private final PinnedProjectVersionsDAO pinnedProjectVersionsDAO;

    @Autowired
    public PinnedVersionService(final HangarVersionsDAO hangarVersionsDAO, final PinnedProjectVersionsDAO pinnedProjectVersionsDAO) {
        this.hangarVersionsDAO = hangarVersionsDAO;
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
        return this.hangarVersionsDAO.getPinnedVersions(projectId);
    }
}
