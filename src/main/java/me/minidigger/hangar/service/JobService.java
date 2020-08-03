package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.JobDao;
import me.minidigger.hangar.db.model.JobsTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final HangarDao<JobDao> jobDao;

    @Autowired
    public JobService(HangarDao<JobDao> jobDao) {
        this.jobDao = jobDao;
    }

    public List<JobsTable> getErroredJobs() {
        return jobDao.get().getErroredJobs();
    }
}
