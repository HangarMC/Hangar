package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RegisterConstructorMapper(ProjectVersionTable.class)
public interface ProjectVersionsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_versions " +
            "(created_at, version_string, description, project_id, channel_id, file_size, hash, file_name, external_url, author_id, create_forum_post) VALUES " +
            "(:now, :versionString, :description, :projectId, :channelId, :fileSize, :hash, :fileName, :externalUrl, :authorId, :createForumPost)")
    ProjectVersionTable insert(@BindBean ProjectVersionTable projectVersionTable);

    @SqlUpdate("UPDATE project_versions SET visibility = :visibility, reviewer_id = :reviewerId, approved_at = :approvedAt, description = :description, " +
            "review_state = :reviewState, external_url = :externalUrl " +
            "WHERE id = :id")
    void update(@BindBean ProjectVersionTable projectVersionsTable);

    @SqlUpdate("DELETE FROM project_versions WHERE id = :id")
    void delete(@BindBean ProjectVersionTable projectVersionTable);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId AND hash = :hash AND version_string = :versionString")
    ProjectVersionTable getProjectVersionTable(long projectId, String hash, String versionString);

    @Timestamped
    @SqlBatch("INSERT INTO project_version_tags (created_at, version_id, name, data, color) VALUES (:now, :versionId, :name, :data, :color)")
    void insertTags(@BindBean Collection<ProjectVersionTagTable> projectVersionTagTables);
}
