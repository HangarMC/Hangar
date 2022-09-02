package io.papermc.hangar.db.dao.internal.table.members;

import io.papermc.hangar.model.db.members.ProjectMemberTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectMemberTable.class)
public interface ProjectMembersDAO extends MembersDAO<ProjectMemberTable> {

    @Override
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_members (project_id, user_id) VALUES (:projectId, :userId)")
    ProjectMemberTable insert(@BindBean ProjectMemberTable table);

    @Override
    @SqlQuery("SELECT * FROM project_members WHERE user_id = :userId AND project_id = :projectId")
    ProjectMemberTable getMemberTable(long projectId, long userId);

    @Override
    @SqlUpdate("DELETE FROM project_members WHERE user_id = :userId AND project_id = :projectId")
    void delete(long projectId, long userId);
}
