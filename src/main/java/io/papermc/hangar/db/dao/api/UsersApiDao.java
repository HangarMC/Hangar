package io.papermc.hangar.db.dao.api;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.modelold.generated.ProjectCompact;
import io.papermc.hangar.modelold.generated.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersApiDao {

    @RegisterBeanMapper(User.class)
    @SqlQuery("SELECT u.id, u.created_at, u.name, u.tagline, u.join_date, array_agg(r.name) roles " +
              " FROM users u" +
              "     JOIN user_global_roles ugr ON u.id = ugr.user_id" +
              "     JOIN roles r ON ugr.role_id = r.id" +
              " WHERE u.name = :name" +
              " GROUP BY u.id")
    User userQuery(String name);

    @RegisterBeanMapper(User.class)
    @SqlQuery("SELECT u.id," +
            "       u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date," +
            "       array_agg(r.name) roles" +
            "   FROM users u" +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE u.name ILIKE '%' || :query || '%' " +
            "   GROUP BY u.id " +
            "   LIMIT :limit OFFSET :offset")
    List<User> usersQuery(String query, long limit, long offset);

    @SqlQuery("SELECT COUNT(*)" +
            "   FROM users u" +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE u.name ILIKE '%' || :query || '%'")
    long usersQueryCount(String query);

    @UseStringTemplateEngine
    @RegisterBeanMapper(ProjectCompact.class)
    @RegisterColumnMapper(PromotedVersionMapper.class)
    @SqlQuery("SELECT p.name," +
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
    List<ProjectCompact> watchersQuery(String user, @Define boolean canSeeHidden, @Define Long userId, String sortOrder, Long limit, long offset);

    @UseStringTemplateEngine
    @RegisterBeanMapper(ProjectCompact.class)
    @RegisterColumnMapper(PromotedVersionMapper.class)
    @SqlQuery("SELECT p.name," +
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
    List<ProjectCompact> starredQuery(String user, @Define boolean canSeeHidden, @Define Long userId, String sortOrder, Long limit, long offset);


}
