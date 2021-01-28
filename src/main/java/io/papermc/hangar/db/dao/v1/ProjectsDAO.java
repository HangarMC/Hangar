package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.model.api.project.Project;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.DefineNamedBindings;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(Project.class)
public interface ProjectsDAO {

    @UseStringTemplateEngine
    @SqlQuery("SELECT p.id," +
            "       p.created_at," +
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
            "         EXISTS(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS starred, " +
            "         EXISTS(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS watching, " +
            "       <else>" +
            "         null as starred," +
            "         null as watching," +
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
            "         <if(slug)> AND (p.slug = :slug) <endif> " +
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
    List<Project> getProjects(String owner, String slug, @Define boolean seeHidden, Long requesterId, String orderBy,
                                            @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<Integer> categories,
                                            @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, //TODO: implement tags with mc_version('data')
                                            String query, @Define String queryStatement, long limit, long offset);

    // This query can be shorter because it doesnt need all those column values as above does, just a single column for the amount of rows to be counted
    @UseStringTemplateEngine
    @SqlQuery("SELECT COUNT(p.id) " +
            "  FROM home_projects p" +
            "         JOIN projects ps ON p.id = ps.id" +
            "         WHERE true " + // Not sure how else to get here a single Where
            "         <if(slug)> AND (p.slug = :slug) <endif> " +
            "         <if(owner)> AND (p.owner_name = :owner) <endif> " +
            "         <if(!seeHidden)> AND (p.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(p.project_members) AND p.visibility != 4)<endif>) <endif> " +
            "         <if(categories)> AND (p.category in (<categories>)) <endif> " +
            "         <if(query)> AND ( <queryStatement> ) <endif> " + // This needs to be in <> because template engine needs to modify the query text
            "         <if(tags)> AND EXISTS ( SELECT pv.tag_name FROM jsonb_to_recordset(p.promoted_versions) " +
            "           AS pv(tag_name TEXT, tag_version TEXT) WHERE (pv.tag_name) in (<tags>) ) <endif> ")
    @DefineNamedBindings
    @AllowUnusedBindings
    long countProjects(String owner, String slug, @Define boolean seeHidden, Long requesterId,
                       @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<Integer> categories,
                       @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, //TODO: implement tags with mc_version('data')
                       String query, @Define String queryStatement);
}
