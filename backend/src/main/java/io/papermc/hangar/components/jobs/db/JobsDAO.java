package io.papermc.hangar.components.jobs.db;

import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(JobTable.class)
public interface JobsDAO {

    @SqlQuery("SELECT * FROM jobs WHERE state = 'fatal_failure' OR (jobs.job_type = 'SCHEDULED_TASK' AND state = 'not_started' AND last_error_descriptor = 'exception') ORDER BY last_updated")
    List<JobTable> getErroredJobs();

    @Timestamped
    @SqlUpdate("INSERT INTO jobs (created_at, last_updated, retry_at, last_error, last_error_descriptor, state, job_type, job_properties) VALUES (:now, :lastUpdated, :retryAt, :lastError, :lastErrorDescriptor, :state, :jobType, :jobProperties)")
    void save(@BindBean JobTable job);

    @SqlQuery("SELECT count(*) FROM jobs WHERE state = 'not_started'")
    long countAwaitingJobs();

    @SqlQuery("UPDATE jobs SET state = 'started', last_updated = now() WHERE id = (" +
        "    SELECT id FROM jobs WHERE state = 'not_started' AND (retry_at IS NULL OR retry_at < now()) ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 1" +
        ") RETURNING *")
    JobTable fetchJob();

    @SqlUpdate("UPDATE jobs SET state = 'done', last_updated = now() WHERE id = :id")
    void finishJob(long id);

    @SqlUpdate("UPDATE jobs SET state = 'not_started', last_updated = now(), retry_at = :retryTime, last_error = :lastError, last_error_descriptor = :lastErrorDescriptor WHERE id = :id")
    void retryIn(long id, OffsetDateTime retryTime, String lastError, String lastErrorDescriptor);

    @SqlUpdate("UPDATE jobs SET state = 'fatal_failure', last_updated = now(), last_error = :lastError, last_error_descriptor = :lastErrorDescriptor WHERE id = :id")
    void fail(long id, String lastError, String lastErrorDescriptor);

    @SqlQuery("SELECT count(*) from jobs WHERE job_type = 'SCHEDULED_TASK' and job_properties->>'taskName' = :taskName")
    long exists(String taskName);

    @SqlQuery("SELECT * FROM jobs WHERE id = :jobId")
    JobTable getJob(long jobId);

    @SqlUpdate("""
        UPDATE jobs
        SET state = 'not_started'
        WHERE state = 'started' AND (last_updated < now() - INTERVAL '15 minutes');
        """)
    long fixStuckJobs();
}
