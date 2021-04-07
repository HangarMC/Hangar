package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(ProjectTable.class)
public interface ProjectsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("insert into projects (created_at, name, slug, owner_name, owner_id, category, description, visibility, homepage, issues, source, support, keywords, license_name, license_url, donation_enabled, donation_email, donation_default_amount, donation_monthly_amounts, donation_onetime_amounts) " +
            "values (:now, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility, :homepage, :issues, :source, :support, :keywords, :licenseName, :licenseUrl, :donationEnabled, :donationEmail, :donationDefaultAmount, :donationMonthlyAmounts, :donationOnetimeAmounts)")
    ProjectTable insert(@BindBean ProjectTable project);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE projects SET name = :name, slug = :slug, category = :category, keywords = :keywords, issues = :issues, source = :source, " +
            "license_name = :licenseName, license_url = :licenseUrl, forum_sync = :forumSync, description = :description, visibility = :visibility, " +
            "support = :support, homepage = :homepage, donation_enabled = :donationEnabled, donation_email = :donationEmail, " +
            "donation_default_amount = :donationDefaultAmount, donation_onetime_amounts = :donationOnetimeAmounts, donation_monthly_amounts = :donationMonthlyAmounts WHERE id = :id")
    ProjectTable update(@BindBean ProjectTable project);

    @SqlUpdate("DELETE FROM projects WHERE id = :id")
    void delete(@BindBean ProjectTable project);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectTable getBySlug(String author, String slug);

    @SqlQuery("SELECT * FROM projects WHERE id = :projectId")
    ProjectTable getById(long projectId);

    @SqlQuery("SELECT * FROM projects WHERE owner_id = :userId")
    List<ProjectTable> getUserProjects(long userId);

    @SqlQuery("SELECT * FROM " +
            "     (SELECT CASE " +
            "         WHEN \"name\" = :name THEN 'OWNER_NAME'" +
            "         WHEN slug = :slug THEN 'OWNER_SLUG'" +
            "     END" +
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
