package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.generated.ProjectStatsAll;
import me.minidigger.hangar.model.viewhelpers.ProjectMember;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.service.project.ProjectFactory.InvalidProjectReason;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(ProjectsTable.class)
public interface ProjectDao {

    @SqlUpdate("insert into projects (created_at, plugin_id, name, slug, owner_name, owner_id, category, description, visibility) " +
               "values (:now, :pluginId, :name, :slug, :ownerName,:ownerId, :category, :description, :visibility)")
    @Timestamped
    @GetGeneratedKeys
    ProjectsTable insert(@BindBean ProjectsTable project);

    // TODO expand as needed
    @SqlUpdate("UPDATE projects SET name = :name, slug = :slug, category = :category, keywords = :keywords, issues = :issues, source = :source, " +
            "license_name = :licenseName, license_url = :licenseUrl, forum_sync = :forumSync, description = :description, visibility = :visibility WHERE id = :id")
    void update(@BindBean ProjectsTable project);

    @SqlUpdate("DELETE FROM projects WHERE id = :id")
    void delete(@BindBean ProjectsTable project);

    @SqlQuery("SELECT CASE " +
              "WHEN owner_id = :ownerId AND name = :name THEN 'OWNER_NAME' " +
              "WHEN owner_id = :ownerId AND slug = :slug THEN 'OWNER_SLUG' " +
              "WHEN plugin_id = :pluginId THEN 'PLUGIN_ID' " +
              "END " +
              "FROM projects"
    )
    InvalidProjectReason checkValidProject(long ownerId, String pluginId, String name, String slug);

    @SqlQuery("SELECT CASE " +
            "WHEN owner_id = :ownerId AND name = :name THEN 'OWNER_NAME' " +
            "WHEN owner_id = :ownerId AND slug = :slug THEN 'OWNER_SLUG' " +
            "END " +
            "FROM projects")
    InvalidProjectReason checkNamespace(long ownerId, String name, String slug);

    @SqlQuery("select * from projects where lower(owner_name) = lower(:author) AND lower(slug) = lower(:slug)")
    ProjectsTable getBySlug(String author, String slug);

    @SqlQuery("SELECT * FROM projects WHERE plugin_id = :pluginId")
    ProjectsTable getByPluginId(String pluginId);

    @SqlQuery("SELECT COUNT(*) FROM projects WHERE owner_id = :id")
    int getProjectCountByUserId(long id);

    @SqlQuery("SELECT * FROM projects WHERE owner_id = :id")
    List<ProjectsTable> getProjectsByUserId(long id);

    @RegisterBeanMapper(ProjectStatsAll.class)
    @SqlQuery("SELECT * FROM " +
              "(SELECT COUNT(*) as watchers FROM project_watchers pw WHERE pw.project_id = :id) as w, " +
              "(SELECT COUNT(*) as stars FROM project_stars ps WHERE ps.project_id = :id) as s, " +
              "(SELECT COUNT(*) as views FROM project_views_individual pvi WHERE pvi.project_id = :id) as v, " +
              "(SELECT COUNT(*) as downloads FROM project_versions_downloads_individual pvdi WHERE pvdi.project_id = :id) as d"
    )
    ProjectStatsAll getProjectStats(long id);

    @RegisterBeanMapper(value = ScopedProjectData.class)
    @RegisterBeanMapper(value = Permission.class, prefix = "perm")
    @SqlQuery("SELECT watching, starred, uproject_flags, coalesce(perm_value, B'0'::BIT(64))::BIGINT perm_value FROM" +
              "(SELECT exists(SELECT 1 FROM project_watchers WHERE project_id = :projectId AND user_id = :userId) as watching) as is_watching," +
              "(SELECT exists(SELECT 1 FROM project_stars WHERE project_id = :projectId AND user_id = :userId) as starred) as is_starred," +
              "(SELECT exists(SELECT 1 FROM project_flags WHERE project_id = :projectId AND user_id = :userId AND is_resolved IS FALSE) as uproject_flags) as is_flagged," +
              "(SELECT permission perm_value FROM project_trust WHERE project_id = :projectId AND user_id = :userId) as perm_table")
    ScopedProjectData getScopedProjectData(long projectId, long userId);


    @RegisterBeanMapper(value = UsersTable.class, prefix = "usr")
    @RegisterBeanMapper(value = ProjectMember.class, prefix = "pm")
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
              "upr.role_type pm_role," +
              "upr.is_accepted pm_is_accepted " +
              "FROM user_project_roles upr JOIN users u on upr.user_id = u.id WHERE upr.project_id = :projectId")
    Map<ProjectMember, UsersTable> getProjectMembers(long projectId);


    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "upr")
    @SqlQuery("SELECT p.*, upr.id upr_id, upr.created_at upr_created_at, upr.user_id upr_user_id, upr.role_type upr_role_type, upr.project_id upr_project_id, upr.is_accepted upr_is_accepted FROM user_project_roles upr JOIN projects p ON p.id = upr.project_id JOIN roles r ON upr.role_type = r.name WHERE upr.user_id = :userId")
    Map<ProjectsTable, UserProjectRolesTable> getProjectsAndRoles(long userId);
}
