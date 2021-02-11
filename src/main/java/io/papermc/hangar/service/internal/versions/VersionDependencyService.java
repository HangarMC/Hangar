package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDependencyDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionPlatformDependencyDAO;
import io.papermc.hangar.model.db.versions.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionDependencyService extends HangarService {

    private final ProjectVersionDependencyDAO projectVersionDependencyDAO;
    private final ProjectVersionPlatformDependencyDAO projectVersionPlatformDependencyDAO;
    private final PlatformVersionDAO platformVersionDAO;

    public VersionDependencyService(HangarDao<ProjectVersionDependencyDAO> projectVersionDependencyDAO, HangarDao<ProjectVersionPlatformDependencyDAO> projectVersionPlatformDependencyDAO, HangarDao<PlatformVersionDAO> platformVersionDAO) {
        this.projectVersionDependencyDAO = projectVersionDependencyDAO.get();
        this.projectVersionPlatformDependencyDAO = projectVersionPlatformDependencyDAO.get();
        this.platformVersionDAO = platformVersionDAO.get();
    }

    public List<ProjectVersionDependencyTable> getProjectVersionDependencyTables(long versionId) {
        return projectVersionDependencyDAO.getForVersion(versionId);
    }

    public List<ProjectVersionPlatformDependencyTable> getProjectVersionPlatformDependencyTable(long versionId) {
        return projectVersionPlatformDependencyDAO.getForVersion(versionId);
    }

}
