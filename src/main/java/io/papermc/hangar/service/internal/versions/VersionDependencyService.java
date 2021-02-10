package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionDependencyDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionPlatformDependencyDAO;
import io.papermc.hangar.db.daoold.PlatformVersionsDao;
import io.papermc.hangar.db.modelold.PlatformVersionsTable;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.ProjectVersionDependencyTable;
import io.papermc.hangar.model.db.versions.ProjectVersionPlatformDependencyTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VersionDependencyService extends HangarService {

    private final ProjectVersionDependencyDAO projectVersionDependencyDAO;
    private final ProjectVersionPlatformDependencyDAO projectVersionPlatformDependencyDAO;
    private final HangarDao<PlatformVersionsDao> platformVersionsDao; // TODO new DAO

    public VersionDependencyService(HangarDao<ProjectVersionDependencyDAO> projectVersionDependencyDAO, HangarDao<ProjectVersionPlatformDependencyDAO> projectVersionPlatformDependencyDAO, HangarDao<PlatformVersionsDao> platformVersionsDao) {
        this.projectVersionDependencyDAO = projectVersionDependencyDAO.get();
        this.projectVersionPlatformDependencyDAO = projectVersionPlatformDependencyDAO.get();
        this.platformVersionsDao = platformVersionsDao;
    }

    public List<ProjectVersionDependencyTable> getProjectVersionDependencyTables(long versionId) {
        return projectVersionDependencyDAO.getForVersion(versionId);
    }

    public List<ProjectVersionPlatformDependencyTable> getProjectVersionPlatformDependencyTable(long versionId) {
        return projectVersionPlatformDependencyDAO.getForVersion(versionId);
    }

    // TODO NEW MODEL
    public Map<Platform, List<PlatformVersionsTable>> getProjectVersionPlatformDependencies(long versionId) {
        return platformVersionsDao.get().getPlatformVersionsForVersion(versionId).stream().collect(Collectors.groupingBy(PlatformVersionsTable::getPlatform));
    }
}
