package io.papermc.hangar.db.daoold;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMembersDao {

    @SqlUpdate("DELETE FROM project_members WHERE project_id = :projectId AND user_id = :userId")
    int delete(long projectId, long userId);
}
