package io.papermc.hangar.db.dao.api;

import io.papermc.hangar.db.mappers.DependencyMapper;
import io.papermc.hangar.db.mappers.PlatformDependencyMapper;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.RolesTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface V1ApiDao {

    @UseStringTemplateEngine
    @SqlQuery("SELECT p.id " +
              "     FROM home_projects p" +
              "     WHERE true" +
              "         <if(q)>AND p.name ILIKE '<q>' OR p.description ILIKE '<q>' OR p.owner_name ILIKE '<q>' <endif>" +
              "         <if(categories)>AND p.category IN (<categories>) <endif>" +
              "     ORDER BY :strategyFragment" +
              "     LIMIT :limit OFFSET :offset")
    List<Long> apiIdSearch(@Define String q, @BindList(onEmpty = EmptyHandling.NULL_VALUE) List<Integer> categories, String strategyFragment, long limit, long offset);

    @UseStringTemplateEngine
    @RegisterBeanMapper(ProjectsTable.class)
    @SqlQuery("SELECT pr.* " +
            "     FROM home_projects p" +
            "     JOIN projects pr ON p.project_id = pr.project_id" +
            "     WHERE true" +
            "         <if(q)>AND p.name ILIKE '<q>' OR p.description ILIKE '<q>' OR p.owner_name ILIKE '<q>' <endif>" +
            "         <if(categories)>AND p.category IN (<categories>) <endif>" +
            "     ORDER BY :strategyFragment" +
            "     LIMIT :limit OFFSET :offset")
    List<ProjectsTable> getProjects(@Define String q, @BindList(onEmpty = EmptyHandling.NULL_VALUE) List<Integer> categories, String strategyFragment, long limit, long offset);

    @RegisterBeanMapper(ProjectsTable.class)
    @SqlQuery("SELECT * FROM projects WHERE owner_id IN (<userIds>)")
    List<ProjectsTable> getProjectsForUsers(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> userIds);

    @UseStringTemplateEngine
    @RegisterBeanMapper(ProjectVersionsTable.class)
    @RegisterColumnMapper(DependencyMapper.class)
    @RegisterColumnMapper(PlatformDependencyMapper.class)
    @SqlQuery("SELECT pv.* " +
              "     FROM project_versions pv" +
              "         JOIN projects p ON pv.project_id = p.id" +
              "         JOIN project_channels pc ON pv.channel_id = pc.id" +
              "     WHERE p.slug = :slug AND p.owner_name = :author" +
              "     <if(channels)>AND lower(pc.name) IN (<channels>)<endif>" +
              "     <if(onlyPublic)>AND pv.visibility = 0<endif>" +
              "     ORDER BY pv.created_at DESC ")
    List<ProjectVersionsTable> getProjectVersions(@BindList(onEmpty = EmptyHandling.NULL_VALUE) List<String> channels, String author, String slug, long limit, long offset, @Define boolean onlyPublic);

    @KeyColumn("owner_id")
    @ValueColumn("slug")
    @SqlQuery("SELECT p.owner_id owner_id, p.slug slug FROM project_stars JOIN projects p ON p.id = project_stars.project_id WHERE p.owner_id in (<userIds>)")
    List<Map.Entry<Long, String>> getStarredPlugins(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> userIds);

    @KeyColumn("user_id")
    @RegisterBeanMapper(RolesTable.class)
    @SqlQuery("SELECT ugr.user_id user_id, r.id, r.name, r.category, r.title, r.color, r.is_assignable, r.rank, r.permission::BIGINT FROM user_global_roles ugr JOIN roles r ON r.id = ugr.role_id WHERE user_id in (<userIds>)")
    List<Map.Entry<Long, RolesTable>> getUsersGlobalRoles(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> userIds);

    @KeyColumn("p_id")
    @RegisterBeanMapper(ProjectChannelsTable.class)
    @SqlQuery("SELECT project_id p_id, * FROM project_channels WHERE project_id IN (<projectIds>)")
    List<Map.Entry<Long, ProjectChannelsTable>> getProjectsChannels(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> projectIds);

    @KeyColumn("p_id")
    @RegisterBeanMapper(ProjectVersionsTable.class)
    @RegisterColumnMapper(DependencyMapper.class)
    @RegisterColumnMapper(PlatformDependencyMapper.class)
    @SqlQuery("SELECT p.id p_id, pv.* FROM project_versions pv JOIN projects p ON pv.project_id = p.id WHERE p.recommended_version_id = pv.id AND p.id IN (<projectIds>)")
    Map<Long, ProjectVersionsTable> getProjectsRecommendedVersion(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> projectIds);

    @UseStringTemplateEngine
    @KeyColumn("p_id")
    @RegisterBeanMapper(ProjectChannelsTable.class)
    @SqlQuery("SELECT p.id p_id, pc.* FROM project_channels pc JOIN project_versions pv ON pc.id = pv.channel_id JOIN projects p ON p.id = pc.project_id WHERE p.id IN (<projectIds>) AND p.recommended_version_id = pv.id")
    Map<Long, ProjectChannelsTable> getProjectsRecommendedVersionChannel(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> projectIds);

    @UseStringTemplateEngine
    @KeyColumn("p_id")
    @RegisterBeanMapper(ProjectChannelsTable.class)
    @SqlQuery("SELECT pv.id p_id, pc.* FROM project_versions pv JOIN project_channels pc ON pv.channel_id = pc.id WHERE pv.id IN (<versionIds>)")
    Map<Long, ProjectChannelsTable> getProjectVersionChannels(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> versionIds);

    @RegisterBeanMapper(value = UserProjectRolesTable.class, prefix = "upr")
    @RegisterBeanMapper(UsersTable.class)
    @SqlQuery("SELECT u.*, upr.id upr_id, upr.created_at upr_created_at, upr.user_id upr_user_id, upr.role_type upr_role_type, upr.project_id upr_project_id, upr.is_accepted upr_is_accepted " +
              "FROM user_project_roles upr " +
              "JOIN users u ON upr.user_id = u.id " +
              "WHERE upr.is_accepted AND upr.project_id IN (<projectIds>)")
    List<Map.Entry<UserProjectRolesTable, UsersTable>> getProjectsMembers(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> projectIds);

    @KeyColumn("pv_id")
    @RegisterBeanMapper(ProjectVersionTagsTable.class)
    @SqlQuery("SELECT pv.id pv_id, pvt.* FROM project_version_tags pvt JOIN project_versions pv ON pv.id = pvt.version_id WHERE pv.visibility = 0 AND pv.id IN (<versionIds>)")
    List<Map.Entry<Long, ProjectVersionTagsTable>> getVersionsTags(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> versionIds);

    @RegisterBeanMapper(UsersTable.class)
    @SqlQuery("SELECT * FROM users OFFSET :offset LIMIT :limit")
    List<UsersTable> getUsers(int offset, Integer limit);
}
