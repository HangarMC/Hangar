package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.modelold.viewhelpers.ProjectMissingFile;
import io.papermc.hangar.modelold.viewhelpers.ScopedProjectData;
import io.papermc.hangar.modelold.viewhelpers.UnhealthyProject;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Deprecated(forRemoval = true)
@RegisterBeanMapper(ProjectsTable.class)
public interface ProjectDao {

    // TODO expand as needed
    @SqlUpdate("UPDATE projects SET name = :name, slug = :slug, category = :category, keywords = :keywords, issues = :issues, source = :source, " +
            "license_name = :licenseName, license_url = :licenseUrl, forum_sync = :forumSync, description = :description, visibility = :visibility, " +
            "recommended_version_id = :recommendedVersionId, notes = :notes, support = :support, homepage = :homepage WHERE id = :id")
    void update(@BindBean ProjectsTable project);

    @SqlUpdate("UPDATE projects SET notes = cast(:notes as jsonb) WHERE id = :id") //TODO check why we have to cast and can't just update (this works tho)
    void updateNotes(String notes, long id);

    @SqlUpdate("DELETE FROM projects WHERE id = :id")
    void delete(@BindBean ProjectsTable project);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectsTable getBySlug(String author, String slug);

    @SqlQuery("SELECT COUNT(*) FROM projects WHERE owner_id = :id")
    int getProjectCountByUserId(long id);

    @RegisterBeanMapper(value = ScopedProjectData.class)
    @RegisterBeanMapper(value = Permission.class, prefix = "perm")
    @SqlQuery("SELECT sq1.watching, sq2.starred, sq3.uproject_flags FROM" +
              "(SELECT exists(SELECT 1 FROM project_watchers WHERE project_id = :projectId AND user_id = :userId) as watching) as sq1," +
              "(SELECT exists(SELECT 1 FROM project_stars WHERE project_id = :projectId AND user_id = :userId) as starred) as sq2," +
              "(SELECT exists(SELECT 1 FROM project_flags WHERE project_id = :projectId AND user_id = :userId AND is_resolved IS FALSE) as uproject_flags) as sq3")
    ScopedProjectData getScopedProjectData(long projectId, long userId);


    @RegisterBeanMapper(value = UsersTable.class, prefix = "usr")
    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "pr")
    @SqlQuery("SELECT " +
              "u.id usr_id," +
              "u.created_at usr_created_at, " +
              "u.full_name usr_full_name," +
              "u.name usr_name," +
              "u.email usr_email," +
              "u.tagline usr_tagline," +
              "u.join_date usr_join_date," +
              "u.read_prompts usr_read_prompts," +
              "u.is_locked usr_is_locked," +
              "u.language usr_language," +
              "upr.id pr_id," +
              "upr.created_at pr_created_at," +
              "upr.user_id pr_user_id," +
              "upr.role_type pr_role_type," +
              "upr.project_id pr_project_id," +
              "upr.accepted pr_accepted " +
              "FROM user_project_roles upr JOIN users u on upr.user_id = u.id WHERE upr.project_id = :projectId")
    Map<UserProjectRolesTable, UsersTable> getProjectMembers(long projectId);


    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "upr")
    @SqlQuery("SELECT p.*, upr.id upr_id, upr.created_at upr_created_at, upr.user_id upr_user_id, upr.role_type upr_role_type, upr.project_id upr_project_id, upr.accepted upr_accepted FROM user_project_roles upr JOIN projects p ON p.id = upr.project_id JOIN roles r ON upr.role_type = r.name WHERE upr.user_id = :userId")
    Map<ProjectsTable, UserProjectRolesTable> getProjectsAndRoles(long userId);


    @RegisterBeanMapper(UnhealthyProject.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT p.owner_name pn_owner, p.slug pn_slug, p.topic_id, p.post_id, coalesce(hp.last_updated, p.created_at), p.visibility" +
            "  FROM projects p JOIN home_projects hp ON p.id = hp.id" +
            "  WHERE p.topic_id IS NULL" +
            "     OR p.post_id IS NULL" +
            "     OR hp.last_updated > (now() - make_interval(secs := <age>))" +
            "     OR p.visibility != 0")
    List<UnhealthyProject> getUnhealthyProjects(@Define("age") long staleAgeSeconds);

    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "pr")
    @SqlQuery("SELECT " +
            "   upr.id pr_id," +
            "   upr.created_at pr_created_at," +
            "   upr.user_id pr_user_id," +
            "   upr.role_type pr_role_type," +
            "   upr.project_id pr_project_id," +
            "   upr.accepted pr_accepted, " +
            "   p.* " +
            "FROM user_project_roles upr " +
            "   JOIN projects p ON p.id = upr.project_id " +
            "WHERE upr.user_id = :userId")
    Map<UserProjectRolesTable, ProjectsTable> getProjectRoles(long userId);

    @SqlQuery("SELECT v.version_string version_versionString, v.file_name version_fileName, p.owner_name AS owner, p.name AS name " +
            "FROM project_versions v JOIN projects p on v.project_id = p.id ")
    @RegisterBeanMapper(value = ProjectMissingFile.class)
    List<ProjectMissingFile> allProjectsForMissingFiles();

}
