package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.JobsDAO;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService extends HangarService {

    private final JobsDAO jobsDAO;

    @Autowired
    public JobService(HangarDao<JobsDAO> jobsDAO) {
        this.jobsDAO = jobsDAO.get();
    }

    public List<JobTable> getErroredJobs() {
        return jobsDAO.getErroredJobs();
    }

    // TODO the 4 kinds of discourse-related jobs
}
