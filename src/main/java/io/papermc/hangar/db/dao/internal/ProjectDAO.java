package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.db.projects.ProjectTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectTable.class)
public interface ProjectDAO {

    @SqlUpdate("insert into projects (created_at, name, slug, owner_name, owner_id, category, description, visibility, homepage, issues, source, support, keywords, license_name, license_url) " +
            "values (:now, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility, :homepage, :issues, :source, :support, :keywords, :licenseName, :licenseUrl)")
    @Timestamped
    @GetGeneratedKeys
    ProjectTable insert(@BindFields ProjectTable project);

    @SqlUpdate("UPDATE projects SET name = :name, slug = :slug, category = :category, keywords = :keywords, issues = :issues, source = :source, " +
            "license_name = :licenseName, license_url = :licenseUrl, forum_sync = :forumSync, description = :description, visibility = :visibility, " +
            "recommended_version_id = :recommendedVersionId, notes = :notes, support = :support, homepage = :homepage WHERE id = :id")
    void update(@BindFields ProjectTable project);

    @SqlUpdate("DELETE FROM projects WHERE id = :id")
    void delete(@BindFields ProjectTable project);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectTable getBySlug(String author, String slug);

    @SqlQuery("SELECT * FROM projects WHERE id = :projectId")
    ProjectTable getById(long projectId);
}
