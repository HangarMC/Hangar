package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.internal.job.Job;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(JobTable.class)
public interface JobsDAO {

    @SqlQuery("SELECT * FROM jobs WHERE state = 'fatal_failure'")
    List<JobTable> getErroredJobs();

    @Timestamped
    @SqlUpdate("INSERT INTO jobs (created_at, last_updated, retry_at, last_error, last_error_descriptor, state, job_type, job_properties) VALUES (:now, :lastUpdated, :retryAt, :lastError, :lastErrorDescriptor, :state, :jobType, :jobProperties)")
    void save(@BindBean JobTable job);
}
