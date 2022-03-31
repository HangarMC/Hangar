package io.papermc.hangar.db.dao;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralDao {

    @SqlUpdate("REFRESH MATERIALIZED VIEW home_projects;")
    void refreshHomeProjects();
}
