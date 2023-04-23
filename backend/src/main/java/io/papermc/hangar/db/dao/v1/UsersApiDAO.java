package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.requests.RequestPagination;
import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersApiDAO {

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
        " p.id," +
        " p.name," +
        " p.owner_name \"owner\"," +
        " p.slug," +
        " hp.views," +
        " hp.downloads," +
        " hp.recent_views," +
        " hp.recent_downloads," +
        " hp.stars," +
        " hp.watchers," +
        " p.category," +
        " coalesce(hp.last_updated, p.created_at) AS last_updated," +
        " p.visibility" +
        " FROM users u " +
        "     JOIN project_stars ps ON u.id = ps.user_id" +
        "     JOIN home_projects hp ON ps.project_id = hp.id" +
        "     JOIN projects p ON ps.project_id = p.id" +
        " WHERE " +
        "     <if(!canSeeHidden)> (p.visibility = 0" +
        "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
        "     lower(u.name) = lower(:user)" +
        " ORDER BY <sortOrder> LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserStarred(String user, @Define boolean canSeeHidden, @Define Long userId, @Define String sortOrder, long limit, long offset);

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
        " FROM users u " +
        "     JOIN project_stars ps ON u.id = ps.user_id" +
        "     JOIN home_projects hp ON ps.project_id = hp.id" +
        "     JOIN projects p ON ps.project_id = p.id" +
        " WHERE " +
        "     <if(!canSeeHidden)> (p.visibility = 0" +
        "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
        "     lower(u.name) = lower(:user)")
    long getUserStarredCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT p.created_at," +
        " p.id," +
        " p.name," +
        " p.owner_name \"owner\"," +
        " p.slug," +
        " hp.views," +
        " hp.downloads," +
        " hp.recent_views," +
        " hp.recent_downloads," +
        " hp.stars," +
        " hp.watchers," +
        " p.category," +
        " coalesce(hp.last_updated, p.created_at) AS last_updated," +
        " p.visibility" +
        " FROM users u " +
        "     JOIN project_watchers pw ON u.id = pw.user_id" +
        "     JOIN home_projects hp ON pw.project_id = hp.id" +
        "     JOIN projects p ON pw.project_id = p.id" +
        " WHERE " +
        "     <if(!canSeeHidden)> (p.visibility = 0" +
        "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
        "     lower(u.name) = lower(:user)" +
        " ORDER BY <sortOrder> LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserWatching(String user, @Define boolean canSeeHidden, @Define Long userId, @Define String sortOrder, long limit, long offset);

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
        " FROM users u " +
        "     JOIN project_watchers pw ON u.id = pw.user_id" +
        "     JOIN home_projects hp ON pw.project_id = hp.id" +
        "     JOIN projects p ON pw.project_id = p.id" +
        " WHERE " +
        "     <if(!canSeeHidden)> (p.visibility = 0" +
        "     <if(userId)>OR (<userId> = ANY(hp.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
        "     lower(u.name) = lower(:user)")
    long getUserWatchingCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @UseStringTemplateEngine
    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.locked," +
        "       array(SELECT r.id FROM roles r JOIN user_global_roles ugr ON r.id = ugr.role_id WHERE u.id = ugr.user_id ORDER BY r.permission::bigint DESC) AS roles," +
        "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count" +
        "   FROM users u" +
        "   WHERE u.id IN " +
        "       (SELECT DISTINCT p.owner_id FROM projects p WHERE p.visibility != 1)" +
        "   <if(hasQuery)> AND u.name ILIKE '%' || :query || '%'<endif>" +
        "   <sorters>" +
        "   <offsetLimit>")
    List<User> getAuthors(@Define boolean hasQuery, String query, @BindPagination RequestPagination pagination);

    @SqlQuery("SELECT count(DISTINCT p.owner_id) FROM projects p WHERE p.visibility != 1")
    long getAuthorsCount();

    @UseStringTemplateEngine
    @RegisterConstructorMapper(User.class)
    @SqlQuery(" SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.locked," +
        "       array_agg(r.id ORDER BY r.permission::bigint DESC) AS roles," +
        "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count" +
        "   FROM users u" +
        "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
        "       JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE r.name IN (<staffRoles>)" +
        "   <if(hasQuery)> AND u.name ILIKE '%' || :query || '%'<endif>" +
        "   GROUP BY u.id" +
        "   <sorters>" +
        "   <offsetLimit>")
    List<User> getStaff(@Define boolean hasQuery, String query, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> staffRoles, @BindPagination RequestPagination pagination);

    @SqlQuery(" SELECT count(u.id)" +
        "   FROM users u " +
        "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
        "       JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE r.name IN (<staffRoles>)")
    long getStaffCount(@BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> staffRoles);

    @RegisterConstructorMapper(UserNameChange.class)
    @SqlQuery("""
            SELECT uh.old_name, uh.new_name, uh.date
            FROM users_history uh
                     JOIN users u ON uh.uuid = u.uuid
            WHERE lower(u.name) = lower(:name) AND uh.date >= :date
            ORDER BY date DESC
        """)
    List<UserNameChange> getUserNameHistory(@NotNull String name, @NotNull OffsetDateTime date);
}
