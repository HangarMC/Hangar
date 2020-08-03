package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.JobsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(JobsTable.class)
public interface JobDao {

    @SqlQuery("SELECT * FROM jobs WHERE state = 'fatal_failure'")
    List<JobsTable> getErroredJobs();
}
