package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(ProjectVersionTable.class)
public interface ProjectVersionsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_versions " +
        "(created_at, version_string, description, project_id, channel_id, author_id, visibility, platforms) VALUES " +
        "(:now, :versionString, :description, :projectId, :channelId, :authorId, :visibility, :platforms)")
    ProjectVersionTable insert(@BindBean ProjectVersionTable projectVersionTable);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE project_versions SET version_string = :versionString, visibility = :visibility, reviewer_id = :reviewerId, approved_at = :approvedAt, " +
        "description = :description, review_state = :reviewState, platforms = :platforms " +
        "WHERE id = :id")
    ProjectVersionTable update(@BindBean ProjectVersionTable projectVersionsTable);

    @SqlUpdate("DELETE FROM project_versions WHERE id = :id")
    void delete(@BindBean ProjectVersionTable projectVersionTable);

    @SqlQuery("SELECT * FROM project_versions pv WHERE pv.id = :versionId")
    ProjectVersionTable getProjectVersionTable(long versionId);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId")
    List<ProjectVersionTable> getProjectVersions(long projectId);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId AND version_string = :versionString")
    ProjectVersionTable getProjectVersion(long projectId, String versionString);

    @SqlQuery("SELECT pv.* FROM project_versions pv" +
        "   WHERE" +
        "       pv.project_id = :id AND" +
        "       pv.version_string = :versionString" +
        "   LIMIT 1")
    ProjectVersionTable getProjectVersionTableWithProjectId(long id, String versionString);

    @SqlQuery("SELECT pv.* FROM project_versions pv" +
        "   JOIN projects p ON pv.project_id = p.id" +
        "   WHERE" +
        "       lower(p.slug) = lower(:slug) AND" +
        "       pv.version_string = :versionString" +
        "   LIMIT 1")
    ProjectVersionTable getProjectVersionTableWithProjectSlug(String slug, String versionString);
}
