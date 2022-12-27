package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.JobTable;
import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(JobTable.class)
public interface JobsDAO {

    @SqlQuery("SELECT * FROM jobs WHERE state = 'fatal_failure'")
    List<JobTable> getErroredJobs();

    @Timestamped
    @SqlUpdate("INSERT INTO jobs (created_at, last_updated, retry_at, last_error, last_error_descriptor, state, job_type, job_properties) VALUES (:now, :lastUpdated, :retryAt, :lastError, :lastErrorDescriptor, :state, :jobType, :jobProperties)")
    void save(@BindBean JobTable job);

    @SqlQuery("SELECT count(*) FROM jobs WHERE state = 'not_started'")
    long countAwaitingJobs();

    @SqlQuery("UPDATE jobs SET state = 'started', last_updated = now() WHERE id = (" +
        "    SELECT id FROM jobs WHERE state = 'not_started' AND (retry_at IS NULL OR retry_at < now()) ORDER BY id /*FOR UPDATE SKIP LOCKED*/ LIMIT 1" +
        ") RETURNING *")
    JobTable fetchJob();

    @SqlUpdate("UPDATE jobs SET state = 'done', last_updated = now() WHERE id = :id")
    void finishJob(long id);

    @SqlUpdate("UPDATE jobs SET state = 'not_started', last_updated = now(), retry_at = :retryTime, last_error = :lastError, last_error_descriptor = :lastErrorDescriptor WHERE id = :id")
    void retryIn(long id, OffsetDateTime retryTime, String lastError, String lastErrorDescriptor);

    @SqlUpdate("UPDATE jobs SET state = 'fatal_failure', last_updated = now(), last_error = :lastError, last_error_descriptor = :lastErrorDescriptor WHERE id = :id")
    void fail(long id, String lastError, String lastErrorDescriptor);
}
