package me.minidigger.hangar.service;

import me.minidigger.hangar.model.generated.ReviewState;
import me.minidigger.hangar.model.viewhelpers.ReviewQueueEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectVersionDao;
import me.minidigger.hangar.db.model.ProjectVersionsTable;

import java.util.List;

@Service
public class VersionService {

    private final HangarDao<ProjectVersionDao> versionDao;

    @Autowired
    public VersionService(HangarDao<ProjectVersionDao> versionDao) {
        this.versionDao = versionDao;
    }

    public ProjectVersionsTable getVersion(long projectId) {
        return versionDao.get().getVersion(projectId);
    }

    public List<ReviewQueueEntry> getReviewQueue() {
        return versionDao.get().getQueue(ReviewState.UNREVIEWED);
    }
}
