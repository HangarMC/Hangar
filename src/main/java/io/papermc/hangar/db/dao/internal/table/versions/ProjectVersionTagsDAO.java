package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.ProjectVersionTagTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RegisterConstructorMapper(ProjectVersionTagTable.class)
public interface ProjectVersionTagsDAO {

    @Timestamped
    @SqlBatch("INSERT INTO project_version_tags (created_at, version_id, name, data, color) VALUES (:now, :versionId, :name, :data, :color)")
    void insertTags(@BindBean Collection<ProjectVersionTagTable> projectVersionTagTables);

    @Timestamped
    @SqlBatch("INSERT INTO project_version_tags (created_at, version_id, name, data, color) VALUES (:now, :versionId, :name, :data, :color)")
    void insertTags(@BindBean ProjectVersionTagTable[] projectVersionTagTables);

    @SqlUpdate("UPDATE project_version_tags SET data = :data WHERE id = :id")
    void updateTag(@BindBean ProjectVersionTagTable projectVersionTagTable);

    @SqlQuery("SELECT * FROM project_version_tags pvt WHERE pvt.version_id = :versionId AND pvt.name = :name")
    ProjectVersionTagTable getTag(long versionId, String name);
}
