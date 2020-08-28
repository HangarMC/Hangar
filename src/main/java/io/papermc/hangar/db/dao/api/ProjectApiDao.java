package io.papermc.hangar.db.dao.api;

import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.generated.Project;

import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.DefineNamedBindings;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(Project.class)
public interface ProjectApiDao {

    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
            "       p.plugin_id," +
            "       p.name," +
            "       p.owner_name," +
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
            "         EXISTS(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS user_stared, " +
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
            "         WHERE true " + //Not sure how else to get here a single Where
            "         <if(pluginId)> AND (p.plugin_id = :pluginId) <endif> " +
            "         <if(owner)> AND (p.owner_name = :owner) <endif> " +
            "         <if(!seeHidden)> AND (p.visibility = 1 OR (:requesterId = ANY(p.project_members) AND p.visibility != 5)) <endif> " +
            "         <if(categories)> AND (p.category in (<categories>)) <endif> " +
            "         <if(query)> AND ( <queryStatement> ) <endif> " +
            "         <if(tags)> AND EXISTS ( SELECT pv.tag_name FROM jsonb_to_recordset(p.promoted_versions) " +
            "           AS pv(tag_name TEXT, tag_version TEXT) WHERE (pv.tag_name) in (<tags>) ) <endif> " +
            "         ORDER BY <orderBy> " +
            "         LIMIT :limit" +
            "         OFFSET :offset")
    @DefineNamedBindings
    List<Project> listProjects(String pluginId, String owner, boolean seeHidden, Long requesterId, @Define String orderBy,
                                  @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) @EnumByOrdinal List<Category> categories,
                                  @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, //TODO: implement tags with mc_version('data')
                                  String query, @Define String queryStatement, long limit, long offset);

    @UseStringTemplateEngine
    @SqlQuery("SELECT COUNT(*) FROM ( SELECT p.created_at," +
            "       p.plugin_id," +
            "       p.name," +
            "       p.owner_name," +
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
            "         EXISTS(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS user_stared, " +
            "         EXISTS(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS user_watching, " +
            "       <endif> "+
            "       ps.homepage," +
            "       ps.issues," +
            "       ps.source," +
            "       ps.support," +
            "       ps.license_name," +
            "       ps.license_url," +
            "       ps.forum_sync" +
            "  FROM home_projects p" +
            "         JOIN projects ps ON p.id = ps.id" +
            "         WHERE true " + //Not sure how else to get here a single Where
            "         <if(pluginId)> AND (p.plugin_id = :pluginId) <endif> " +
            "         <if(owner)> AND (p.owner_name = :owner) <endif> " +
            "         <if(!seeHidden)> AND (p.visibility = 1 OR (:requesterId = ANY(p.project_members) AND p.visibility != 5)) <endif> " +
            "         <if(categories)> AND (p.category in (<categories>)) <endif> " +
            "         <if(query)> AND ( <queryStatement> ) <endif> " +
            "         <if(tags)> AND EXISTS ( SELECT pv.tag_name FROM jsonb_to_recordset(p.promoted_versions) " +
            "           AS pv(tag_name TEXT, tag_version TEXT) WHERE (pv.tag_name) in (<tags>) ) <endif> " +
            "         ) sq")
    @DefineNamedBindings
    long countProjects(String pluginId, String owner, @Define boolean seeHidden, Long requesterId,
                       @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) @EnumByOrdinal List<Category> categories,
                       @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, //TODO: implement tags with mc_version('data')
                       String query, @Define String queryStatement);
}
