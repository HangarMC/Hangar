package io.papermc.hangar.db.dao.api;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.model.generated.Project;
import io.papermc.hangar.model.generated.ProjectMember;
import io.papermc.hangar.model.generated.ProjectStatsDay;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.DefineNamedBindings;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(Project.class)
public interface ProjectsApiDao {

    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
            "       p.plugin_id," +
            "       p.name," +
            "       p.owner_name \"owner\"," +
            "       p.slug," +
            "       p.promoted_versions," +
            "       p.views," +
            "       p.downloads," +
            "       p.recent_views," +
            "       p.recent_downloads," +
            "       p.stars," +
            "       p.watchers," +
            "       p.category," +
            "       p.description," +
            "       COALESCE(p.last_updated, p.created_at) AS last_updated," +
            "       p.visibility, " +
            "       <if(requesterId)> " +
            "         EXISTS(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS user_starred, " +
            "         EXISTS(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS user_watching, " +
            "       <endif>" +
            "       ps.homepage," +
            "       ps.issues," +
            "       ps.source," +
            "       ps.support," +
            "       ps.license_name," +
            "       ps.license_url," +
            "       ps.forum_sync" +
            "  FROM home_projects p" +
            "         JOIN projects ps ON p.id = ps.id" +
            "         WHERE true " + // Not sure how else to get here a single Where
            "         <if(pluginId)> AND (p.plugin_id = :pluginId) <endif> " +
            "         <if(owner)> AND (p.owner_name = :owner) <endif> " +
            "         <if(!seeHidden)> AND (p.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(p.project_members) AND p.visibility != 4)<endif>) <endif> " +
            "         <if(categories)> AND (p.category in (<categories>)) <endif> " +
            "         <if(query)> AND ( <queryStatement> ) <endif> " + // This needs to be in <> because template engine needs to modify the query text
            "         <if(tags)> AND EXISTS ( SELECT pv.tag_name FROM jsonb_to_recordset(p.promoted_versions) " +
            "           AS pv(tag_name TEXT, tag_version TEXT) WHERE (pv.tag_name) in (<tags>) ) <endif> " +
            "         <if(orderBy)>ORDER BY :orderBy<endif> " +
            "         LIMIT :limit" +
            "         OFFSET :offset")
    @RegisterColumnMapper(PromotedVersionMapper.class)
    @DefineNamedBindings
    List<Project> listProjects(String pluginId, String owner, @Define boolean seeHidden, Long requesterId, String orderBy,
                               @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<Integer> categories,
                               @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, //TODO: implement tags with mc_version('data')
                               @Define String query, @Define String queryStatement, long limit, long offset);

    // This query can be shorter because it doesnt need all those column values as above does, just a single column for the amount of rows to be counted
    @UseStringTemplateEngine
    @SqlQuery("SELECT COUNT(p.plugin_id) " +
            "  FROM home_projects p" +
            "         JOIN projects ps ON p.id = ps.id" +
            "         WHERE true " + // Not sure how else to get here a single Where
            "         <if(pluginId)> AND (p.plugin_id = :pluginId) <endif> " +
            "         <if(owner)> AND (p.owner_name = :owner) <endif> " +
            "         <if(!seeHidden)> AND (p.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(p.project_members) AND p.visibility != 4)<endif>) <endif> " +
            "         <if(categories)> AND (p.category in (<categories>)) <endif> " +
            "         <if(query)> AND ( <queryStatement> ) <endif> " + // This needs to be in <> because template engine needs to modify the query text
            "         <if(tags)> AND EXISTS ( SELECT pv.tag_name FROM jsonb_to_recordset(p.promoted_versions) " +
            "           AS pv(tag_name TEXT, tag_version TEXT) WHERE (pv.tag_name) in (<tags>) ) <endif> ")
    @DefineNamedBindings
    @AllowUnusedBindings
    long countProjects(String pluginId, String owner, @Define boolean seeHidden, Long requesterId,
                       @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<Integer> categories,
                       @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, //TODO: implement tags with mc_version('data')
                       @Define String query, @Define String queryStatement);

    @RegisterBeanMapper(ProjectMember.class)
    @SqlQuery("SELECT u.name AS user, array_agg(r.name) roles " +
              " FROM projects p " +
              "     JOIN user_project_roles upr ON p.id = upr.project_id " +
              "     JOIN users u ON upr.user_id = u.id " +
              "     JOIN roles r ON upr.role_type = r.name " +
              " WHERE p.plugin_id = :pluginId" +
              " GROUP BY u.name ORDER BY max(r.permission::BIGINT) DESC " +
              " LIMIT :limit OFFSET :offset")
    List<ProjectMember> projectMembers(String pluginId, long limit, long offset);

    @KeyColumn("dateKey")
    @RegisterBeanMapper(ProjectStatsDay.class)
    @SqlQuery("SELECT CAST(dates.day AS date) dateKey, coalesce(sum(pvd.downloads), 0) AS downloads, coalesce(pv.views, 0) AS views" +
              " FROM projects p," +
              "     (SELECT generate_series(:startDate::DATE, :endDate::DATE, INTERVAL '1 DAY') AS day) dates" +
              "     LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day" +
              "     LEFT JOIN project_views pv ON dates.day = pv.day AND pvd.project_id = pv.project_id" +
              " WHERE p.plugin_id = :pluginId AND (pvd IS NULL OR pvd.project_id = p.id)" +
              " GROUP BY pv.views, dates.day")
    Map<String, ProjectStatsDay> projectStats(String pluginId, LocalDate startDate, LocalDate endDate);
}
