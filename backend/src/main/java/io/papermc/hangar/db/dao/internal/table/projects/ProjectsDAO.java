package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectTable.class)
public interface ProjectsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO projects (created_at, name, slug, owner_name, owner_id, category, description, visibility, homepage, issues, source, support, wiki, keywords, license_type, license_name, license_url, donation_enabled, donation_subject, sponsors) " +
        "VALUES (:now, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility, :homepage, :issues, :source, :support, :wiki, :keywords, :licenseType, :licenseName, :licenseUrl, :donationEnabled, :donationSubject, :sponsors)")
    ProjectTable insert(@BindBean ProjectTable project);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE projects SET name = :name, slug = :slug, category = :category, keywords = :keywords, issues = :issues, source = :source, " +
        "license_type = :licenseType, license_name = :licenseName, license_url = :licenseUrl, forum_sync = :forumSync, description = :description, visibility = :visibility, " +
        "support = :support, homepage = :homepage, wiki = :wiki, post_id = :postId, topic_id = :topicId, donation_enabled = :donationEnabled, donation_subject = :donationSubject, " +
        "sponsors = :sponsors WHERE id = :id")
    ProjectTable update(@BindBean ProjectTable project);

    @SqlUpdate("UPDATE projects SET owner_name = :ownerName, owner_id = :ownerId WHERE id = :id")
    void updateOwner(@BindBean ProjectTable project);

    @SqlUpdate("DELETE FROM projects WHERE id = :id")
    void delete(@BindBean ProjectTable project);

    @SqlQuery("SELECT * FROM projects WHERE lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectTable getBySlug(String author, String slug);

    @SqlQuery("SELECT * FROM projects WHERE id = :projectId")
    ProjectTable getById(long projectId);

    @UseStringTemplateEngine
    @SqlQuery("SELECT * FROM projects WHERE owner_id = :userId <if(!seeHidden)> AND visibility = 0<endif>")
    List<ProjectTable> getUserProjects(long userId, @Define boolean seeHidden);

    @SqlQuery("SELECT * FROM " +
        "     (SELECT CASE " +
        "         WHEN \"name\" = :name THEN 'OWNER_NAME'" +
        "         WHEN slug = :slug THEN 'OWNER_SLUG'" +
        "     END AS sq" +
        "     FROM projects WHERE owner_id = :ownerId) sq" +
        " WHERE sq IS NOT NULL ")
    ProjectFactory.InvalidProjectReason checkProjectValidity(long ownerId, String name, String slug);

    @RegisterConstructorMapper(UserTable.class)
    @SqlQuery("SELECT u.* " +
        "   FROM project_watchers pw" +
        "   JOIN users u ON pw.user_id = u.id" +
        "   WHERE pw.project_id = :projectId")
    List<UserTable> getProjectWatchers(long projectId);
}
