package io.papermc.hangar.db.dao;


import io.papermc.hangar.db.model.ProjectViewsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RegisterBeanMapper(ProjectViewsTable.class)
// TODO: decide where we do count it as a view and where we don't
public interface ProjectViewDao {

    // The db will automatically get the current_date from itself (if you dont provide any date)
    @SqlUpdate("insert into project_views (day, project_id, views) values (current_date , :projectId, 1)" +
            "on conflict (day, project_id) " +
            "do update set views = project_views.views + 1 ")
    void increaseView(long projectId);

    @SqlQuery("select views from project_views " +
            "where day = :day and project_id = :projectId ")
    long getViewsFromDate(LocalDate day, long projectId);

    @SqlQuery("select day, project_id, views from project_views " +
            "where day = :day and project_id = :projectId ")
    ProjectViewsTable getViewsTable(LocalDate day, long projectId);

}
