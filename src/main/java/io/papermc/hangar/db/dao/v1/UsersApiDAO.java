package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.requests.RequestPagination;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersApiDAO {

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
            " p.name," +
            " p.owner_name \"owner\"," +
            " p.slug," +
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

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects p ON ps.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserStarredCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
            " p.name," +
            " p.owner_name \"owner\"," +
            " p.slug," +
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

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects p ON pw.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserWatchingCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date," +
            "       u.locked," +
            "       array(SELECT r.name FROM roles r JOIN user_global_roles ugr ON r.id = ugr.role_id WHERE u.id = ugr.user_id ORDER BY r.permission::BIGINT DESC) roles," +
            "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count" +
            "   FROM users u" +
            "   WHERE u.id IN " +
            "       (SELECT DISTINCT p.owner_id FROM projects p WHERE p.visibility != 1)" +
            "   <sorters>" +
            "   <offsetLimit>")
    List<User> getAuthors(@BindPagination RequestPagination pagination);

    @SqlQuery("SELECT count(DISTINCT p.owner_id) FROM projects p WHERE p.visibility != 1")
    long getAuthorsCount();

    @RegisterConstructorMapper(User.class)
    @SqlQuery(" SELECT u.id," +
            "       u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date," +
            "       u.locked," +
            "       array_agg(r.name ORDER BY r.permission::BIGINT DESC) roles," +
            "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count" +
            "   FROM users u" +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE r.name IN (<staffRoles>)" +
            "   GROUP BY u.id" +
            "   <sorters>" +
            "   <offsetLimit>")
    List<User> getStaff(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> staffRoles, @BindPagination RequestPagination pagination);

    @SqlQuery(" SELECT count(u.id)" +
            "   FROM users u " +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE r.name in (<staffRoles>)")
    long getStaffCount(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> staffRoles);
}
