package io.papermc.hangar.service;

import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.db.daoold.ProjectVersionDownloadWarningDao;
import io.papermc.hangar.db.daoold.ProjectVersionUnsafeDownloadsDao;
import io.papermc.hangar.db.modelold.ProjectVersionDownloadWarningsTable;
import io.papermc.hangar.db.modelold.ProjectVersionUnsafeDownloadsTable;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class DownloadsService {

    private final HangarDao<ProjectVersionUnsafeDownloadsDao> projectVersionUnsafeDownloadsDao;
    private final HangarDao<ProjectVersionDownloadWarningDao> projectVersionDownloadWarningDao;

    public DownloadsService(HangarDao<ProjectVersionUnsafeDownloadsDao> projectVersionUnsafeDownloadsDao, HangarDao<ProjectVersionDownloadWarningDao> projectVersionDownloadWarningDao) {
        this.projectVersionUnsafeDownloadsDao = projectVersionUnsafeDownloadsDao;
        this.projectVersionDownloadWarningDao = projectVersionDownloadWarningDao;
    }

    public ProjectVersionUnsafeDownloadsTable addUnsafeDownload(ProjectVersionUnsafeDownloadsTable projectVersionUnsafeDownloadsTable) {
        return projectVersionUnsafeDownloadsDao.get().insert(projectVersionUnsafeDownloadsTable);
    }

    public ProjectVersionDownloadWarningsTable findUnconfirmedWarning(InetAddress address, String token, long versionId) {
        return projectVersionDownloadWarningDao.get().findUnconfirmedWarning(address, token, versionId);
    }

    public void deleteWarning(ProjectVersionDownloadWarningsTable projectVersionDownloadWarningsTable) {
        projectVersionDownloadWarningDao.get().delete(projectVersionDownloadWarningsTable);
    }

    public void confirmWarning(ProjectVersionDownloadWarningsTable projectVersionDownloadWarningsTable, long downloadId) {
        projectVersionDownloadWarningsTable.setIsConfirmed(true);
        projectVersionDownloadWarningsTable.setDownloadId(downloadId);
        projectVersionDownloadWarningDao.get().update(projectVersionDownloadWarningsTable);
    }

}
