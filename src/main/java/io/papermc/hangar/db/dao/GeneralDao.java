package io.papermc.hangar.db.dao;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralDao {

    @SqlUpdate("refresh materialized view home_projects;")
    void refreshHomeProjects();
}
