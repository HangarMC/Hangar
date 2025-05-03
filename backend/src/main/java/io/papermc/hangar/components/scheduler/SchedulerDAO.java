package io.papermc.hangar.components.scheduler;

import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
public interface SchedulerDAO {

    @SqlUpdate("REFRESH MATERIALIZED VIEW home_projects")
    void refreshHomeProjects();

    @SqlUpdate("REFRESH MATERIALIZED VIEW version_stats_view")
    void refreshVersionStatsView();

    @SqlUpdate("REFRESH MATERIALIZED VIEW project_stats_view")
    void refreshProjectStatsView();
}
