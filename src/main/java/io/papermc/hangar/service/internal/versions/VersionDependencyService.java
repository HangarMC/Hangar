package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDependenciesDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionPlatformDependenciesDAO;
import io.papermc.hangar.model.db.versions.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionDependencyService extends HangarService {

    private final ProjectVersionDependenciesDAO projectVersionDependenciesDAO;
    private final ProjectVersionPlatformDependenciesDAO projectVersionPlatformDependenciesDAO;
    private final PlatformVersionDAO platformVersionDAO;

    public VersionDependencyService(HangarDao<ProjectVersionDependenciesDAO> projectVersionDependencyDAO, HangarDao<ProjectVersionPlatformDependenciesDAO> projectVersionPlatformDependencyDAO, HangarDao<PlatformVersionDAO> platformVersionDAO) {
        this.projectVersionDependenciesDAO = projectVersionDependencyDAO.get();
        this.projectVersionPlatformDependenciesDAO = projectVersionPlatformDependencyDAO.get();
        this.platformVersionDAO = platformVersionDAO.get();
    }

    public List<ProjectVersionDependencyTable> getProjectVersionDependencyTables(long versionId) {
        return projectVersionDependenciesDAO.getForVersion(versionId);
    }

    public List<ProjectVersionPlatformDependencyTable> getProjectVersionPlatformDependencyTable(long versionId) {
        return projectVersionPlatformDependenciesDAO.getForVersion(versionId);
    }

}
