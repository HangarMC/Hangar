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
    @SqlQuery("SELECT hp.created_at," +
            " hp.name," +
            " hp.owner_name \"owner\"," +
            " hp.slug," +
            " hp.views," +
            " hp.downloads," +
            " hp.recent_views," +
            " hp.recent_downloads," +
            " hp.stars," +
            " hp.watchers," +
            " hp.category," +
            " coalesce(hp.last_updated, hp.created_at) AS last_updated," +
            " hp.visibility" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects hp ON ps.project_id = hp.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (hp.visibility = 0" +
            "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user" +
            " ORDER BY <sortOrder> LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserStarred(String user, @Define boolean canSeeHidden, @Define Long userId, @Define String sortOrder, long limit, long offset);

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects hp ON ps.project_id = hp.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (hp.visibility = 0" +
            "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserStarredCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT hp.created_at," +
            " hp.name," +
            " hp.owner_name \"owner\"," +
            " hp.slug," +
            " hp.views," +
            " hp.downloads," +
            " hp.recent_views," +
            " hp.recent_downloads," +
            " hp.stars," +
            " hp.watchers," +
            " hp.category," +
            " coalesce(hp.last_updated, hp.created_at) AS last_updated," +
            " hp.visibility" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects hp ON pw.project_id = hp.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (hp.visibility = 0" +
            "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user" +
            " ORDER BY <sortOrder> LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserWatching(String user, @Define boolean canSeeHidden, @Define Long userId, @Define String sortOrder, long limit, long offset);

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects hp ON pw.project_id = hp.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (hp.visibility = 0" +
            "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserWatchingCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date," +
            "       u.locked," +
            "       array(SELECT r.name FROM roles r JOIN user_global_roles ugr ON r.id = ugr.role_id WHERE u.id = ugr.user_id ORDER BY r.permission::BIGINT DESC) AS roles," +
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
            "       array_agg(r.name ORDER BY r.permission::BIGINT DESC) AS roles," +
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
