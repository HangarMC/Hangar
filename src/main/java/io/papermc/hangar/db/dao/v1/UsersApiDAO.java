package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.modelold.viewhelpers.Staff;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(ProjectCompact.class)
@RegisterColumnMapper(PromotedVersionMapper.class)
@UseStringTemplateEngine
public interface UsersApiDAO {

    @SqlQuery("SELECT p.created_at," +
            " p.name," +
            " p.owner_name \"owner\"," +
            " p.slug," +
            " p.promoted_versions," +
            " p.views," +
            " p.downloads," +
            " p.recent_views," +
            " p.recent_downloads," +
            " p.stars," +
            " p.watchers," +
            " p.category," +
            " p.visibility" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects p ON ps.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user" +
            " ORDER BY :sortOrder LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserStarred(String user, @Define boolean canSeeHidden, @Define Long userId, String sortOrder, long limit, long offset);

    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects p ON ps.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserStarredCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @SqlQuery("SELECT p.created_at," +
            " p.name," +
            " p.owner_name \"owner\"," +
            " p.slug," +
            " p.promoted_versions," +
            " p.views," +
            " p.downloads," +
            " p.recent_views," +
            " p.recent_downloads," +
            " p.stars," +
            " p.watchers," +
            " p.category," +
            " p.visibility" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects p ON pw.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user" +
            " ORDER BY :sortOrder LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserWatching(String user, @Define boolean canSeeHidden, @Define Long userId, String sortOrder, long limit, long offset);

    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects p ON pw.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserWatchingCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    // TODO fix this query
    @SqlQuery("SELECT sq.name," +
              "       sq.join_date," +
              "       sq.created_at," +
              "       sq.role," +
              "       sq.donator_role," +
              "       sq.count" +
              "    FROM (SELECT u.name," +
              "                 u.join_date," +
              "                 u.created_at," +
              "                 r.name                                                                                           AS role," +
              "                 r.permission," +
              "                 (SELECT COUNT(*)" +
              "                      FROM project_members_all pma" +
              "                      WHERE pma.user_id = u.id)                                                                   AS count," +
              "                 CASE WHEN dr.rank IS NULL THEN NULL ELSE dr.name END                                             AS donator_role," +
              "                 row_number() OVER (PARTITION BY u.id ORDER BY r.permission::BIGINT DESC, dr.rank ASC NULLS LAST) AS row" +
              "              FROM projects p" +
              "                       JOIN users u ON p.owner_id = u.id" +
              "                       LEFT JOIN user_global_roles gr ON gr.user_id = u.id" +
              "                       LEFT JOIN roles r ON gr.role_id = r.id" +
              "                       LEFT JOIN user_global_roles dgr ON dgr.user_id = u.id" +
              "                       LEFT JOIN roles dr ON dgr.role_id = dr.id) sq" +
              "    WHERE sq.row = 1" +
              "    <userOrder>" +
              "    OFFSET :offset LIMIT :limit")
    @UseStringTemplateEngine
    @RegisterBeanMapper(User.class)
    List<User> getAuthors(long offset, long limit, @Define String userOrder);

    @SqlQuery("SELECT count(*)" +
              "    FROM (SELECT u.name," +
              "                 row_number() OVER (PARTITION BY u.id ORDER BY r.permission::BIGINT DESC, dr.rank ASC NULLS LAST) AS row" +
              "              FROM projects p" +
              "                       JOIN users u ON p.owner_id = u.id" +
              "                       LEFT JOIN user_global_roles gr ON gr.user_id = u.id" +
              "                       LEFT JOIN roles r ON gr.role_id = r.id" +
              "                       LEFT JOIN user_global_roles dgr ON dgr.user_id = u.id" +
              "                       LEFT JOIN roles dr ON dgr.role_id = dr.id) sq" +
              "    WHERE sq.row = 1")
    long getAuthorsCount();

    // TODO fix this query
    @SqlQuery("SELECT sq.name, sq.join_date, sq.created_at, sq.role" +
              "  FROM (SELECT u.name                                                  AS name," +
              "               r.name                                                  AS role," +
              "               u.join_date," +
              "               u.created_at," +
              "               r.permission," +
              "               rank() OVER (PARTITION BY u.name ORDER BY r.permission::BIGINT DESC) AS rank" +
              "          FROM users u" +
              "                 JOIN user_global_roles ugr ON u.id = ugr.user_id" +
              "                 JOIN roles r ON ugr.role_id = r.id" +
              "          WHERE r.name IN ('Hangar_Admin', 'Hangar_Mod')) sq" +
              "  WHERE sq.rank = 1" +
              "    <userOrder>" +
              "    OFFSET :offset LIMIT :limit")
    @UseStringTemplateEngine
    @RegisterBeanMapper(User.class)
    List<User> getStaff(long offset, long limit, @Define String userOrder);

    @SqlQuery("SELECT count(*)" +
              "  FROM (SELECT u.name                                                  AS name," +
              "               rank() OVER (PARTITION BY u.name ORDER BY r.permission::BIGINT DESC) AS rank" +
              "          FROM users u" +
              "                 JOIN user_global_roles ugr ON u.id = ugr.user_id" +
              "                 JOIN roles r ON ugr.role_id = r.id" +
              "          WHERE r.name IN ('Hangar_Admin', 'Hangar_Mod')) sq" +
              "  WHERE sq.rank = 1")
    long getStaffCount();
}
