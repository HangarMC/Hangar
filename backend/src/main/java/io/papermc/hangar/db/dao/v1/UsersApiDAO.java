package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.requests.RequestPagination;
import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.jetbrains.annotations.NotNull;

@JdbiRepository
public interface UsersApiDAO {

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT hp.created_at,
         hp.id,
         hp.name,
         hp.owner_name "owner",
         hp.slug,
         hp.views,
         hp.downloads,
         hp.recent_views,
         hp.recent_downloads,
         hp.stars,
         hp.watchers,
         hp.category,
         hp.last_updated,
         hp.visibility,
         hp.avatar,
         hp.avatar_fallback,
         hp.description
         FROM project_stars ps
             JOIN home_projects hp ON ps.project_id = hp.id
         WHERE
             <if(!canSeeHidden)> (hp.visibility = 0
             <if(requesterId)>OR (<requesterId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND <endif>
             ps.user_id = :userId
           <sorters>
           <offsetLimit>""")
    List<ProjectCompact> getUserStarred(long userId, @Define boolean canSeeHidden, @Define Long requesterId, @BindPagination RequestPagination pagination);

    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT count(*)
         FROM project_stars ps
             JOIN home_projects hp ON ps.project_id = hp.id
         WHERE
             <if(!canSeeHidden)> (hp.visibility = 0
             <if(requesterId)>OR (<requesterId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND <endif>
             ps.user_id = :userId""")
    long getUserStarredCount(long userId, @Define boolean canSeeHidden, @Define Long requesterId);

    @RegisterConstructorMapper(ProjectCompact.class)
    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT hp.created_at,
         hp.id,
         hp.name,
         hp.owner_name "owner",
         hp.slug,
         hp.views,
         hp.downloads,
         hp.recent_views,
         hp.recent_downloads,
         hp.stars,
         hp.watchers,
         hp.category,
         hp.last_updated,
         hp.visibility,
         hp.avatar,
         hp.avatar_fallback,
         hp.description
         FROM project_watchers pw
             JOIN home_projects hp ON pw.project_id = hp.id
         WHERE
             <if(!canSeeHidden)> (hp.visibility = 0
             <if(requesterId)>OR (<requesterId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND <endif>
             pw.user_id = :userId
           <sorters>
           <offsetLimit>""")
    List<ProjectCompact> getUserWatching(long userId, @Define boolean canSeeHidden, @Define Long requesterId, @BindPagination RequestPagination pagination);

    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT count(*)
         FROM project_watchers pw
             JOIN home_projects hp ON pw.project_id = hp.id
         WHERE
             <if(!canSeeHidden)> (hp.visibility = 0
             <if(requesterId)>OR (<requesterId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) AND <endif>
             pw.user_id = :userId""")
    long getUserWatchingCount(long userId, @Define boolean canSeeHidden, @Define Long requesterId);

    @UseStringTemplateEngine
    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.locked," +
        "       u.socials, " +
        "       u.avatar_url," +
        "       array(SELECT r.id FROM roles r JOIN user_global_roles ugr ON r.id = ugr.role_id WHERE u.id = ugr.user_id ORDER BY r.permission::bigint DESC) AS roles," +
        "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count" +
        "   FROM users u" +
        "   WHERE u.id IN " +
        "       (SELECT DISTINCT p.owner_id FROM projects p WHERE p.visibility != 1)" +
        "   <if(hasQuery)> AND u.name ILIKE '%' || :query || '%'<endif>" +
        "   <sorters>" +
        "   <offsetLimit>")
    List<User> getAuthors(@Define boolean hasQuery, String query, @BindPagination RequestPagination pagination);

    @AllowUnusedBindings
    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT count(DISTINCT p.owner_id)
        FROM projects p
        <if(hasQuery)> JOIN users u ON p.owner_id = u.id<endif>
        WHERE p.visibility != 1
        <if(hasQuery)> AND u.name ILIKE '%' || :query || '%'<endif>
        """)
    long getAuthorsCount(@Define boolean hasQuery, String query);

    @UseStringTemplateEngine
    @RegisterConstructorMapper(User.class)
    @SqlQuery(" SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.locked," +
        "       u.socials, " +
        "       u.avatar_url," +
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

    @UseStringTemplateEngine
    @SqlQuery("""
         SELECT count(u.id)
           FROM users u
               JOIN user_global_roles ugr ON u.id = ugr.user_id
               JOIN roles r ON ugr.role_id = r.id
           WHERE r.name IN (<staffRoles>)
           <if(hasQuery)> AND u.name ILIKE '%' || :query || '%'<endif>
        """)
    long getStaffCount(@Define boolean hasQuery, String query, @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<String> staffRoles);

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
