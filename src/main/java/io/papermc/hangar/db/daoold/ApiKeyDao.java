package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectApiKeysTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectApiKeysTable.class)
public interface ApiKeyDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_api_keys (created_at, project_id, value) VALUES (:now, :projectId, :value)")
    ProjectApiKeysTable insert(@BindBean ProjectApiKeysTable projectApiKeysTable);

    @SqlUpdate("DELETE FROM project_api_keys WHERE id = :id")
    void delete(@BindBean ProjectApiKeysTable projectApiKeysTable);

    @SqlQuery("SELECT * FROM project_api_keys WHERE id = :id")
    ProjectApiKeysTable getById(long id);

    @SqlQuery("SELECT * FROM project_api_keys pak WHERE pak.project_id = :projectId")
    List<ProjectApiKeysTable> getByProjectId(long projectId);

}
