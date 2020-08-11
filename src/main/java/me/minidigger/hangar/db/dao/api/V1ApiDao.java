package me.minidigger.hangar.db.dao.api;

import me.minidigger.hangar.db.model.ProjectChannelsTable;
import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.RolesTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface V1ApiDao {

    @RegisterBeanMapper(ProjectsTable.class)
    @SqlQuery("SELECT * FROM projects WHERE owner_id IN (<userIds>)")
    List<ProjectsTable> getProjectsForUsers(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> userIds);

    @KeyColumn("owner_id")
    @ValueColumn("plugin_id")
    @SqlQuery("SELECT p.owner_id owner_id, p.plugin_id plugin_id FROM project_stars JOIN projects p ON p.id = project_stars.project_id WHERE p.owner_id in (<userIds>)")
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
    @SqlQuery("SELECT p.id p_id, pv.* FROM project_versions pv JOIN projects p ON pv.project_id = p.id WHERE p.recommended_version_id = pv.id AND p.id IN (<projectIds>)")
    Map<Long, ProjectVersionsTable> getProjectsRecommendedVersion(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> projectIds);

    @KeyColumn("p_id")
    @RegisterBeanMapper(ProjectChannelsTable.class)
    @SqlQuery("SELECT p.id p_id, pc.* FROM project_channels pc JOIN project_versions pv ON pc.id = pv.channel_id JOIN projects p ON p.id = pc.project_id WHERE p.id IN (<projectIds>) AND p.recommended_version_id = pv.id")
    Map<Long, ProjectChannelsTable> getProjectsRecommendedVersionChannel(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<Long> projectIds);

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
}
