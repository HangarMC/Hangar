package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.JobTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(JobTable.class)
public interface JobsDAO {

    @SqlQuery("SELECT * FROM jobs WHERE state = 'fatal_failure'")
    List<JobTable> getErroredJobs();
}
