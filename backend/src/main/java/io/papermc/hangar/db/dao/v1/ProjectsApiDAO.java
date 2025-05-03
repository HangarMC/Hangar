package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.db.mappers.factories.CompactRoleColumnMapperFactory;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.checkerframework.checker.nullness.qual.Nullable;

@JdbiRepository
@RegisterConstructorMapper(Project.class)
public interface ProjectsApiDAO {

    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT p.id,
               p.created_at,
               p.name,
               p.owner_name "owner",
               p.slug,
               hp.views,
               hp.downloads,
               hp.recent_views,
               hp.recent_downloads,
               p.stars,
               p.watchers,
               p.category,
               p.description,
               coalesce(p.last_updated, p.created_at) AS last_updated,
               p.visibility,
               exists(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS starred,
               exists(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS watching,
               exists(SELECT * FROM project_flags pf WHERE pf.project_id = p.id AND pf.user_id = :requesterId AND pf.resolved IS FALSE) AS flagged,
               p.links,
               p.tags,
               p.license_name,
               p.license_url,
               p.license_type,
               p.keywords,
               p.donation_enabled,
               p.donation_subject,
               p.sponsors,
               hp.avatar,
               hp.avatar_fallback,
               hp.supported_platforms
          FROM home_projects hp
                 JOIN projects_extra p ON hp.id = p.id
                 WHERE p.id = :id
                 <if(!canSeeHidden)> AND (p.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(p.project_members) AND p.visibility != 4)<endif>) <endif>
       """)
    Project getProject(long id, @Define boolean canSeeHidden, @Define @Bind Long requesterId);

    @UseStringTemplateEngine
    @SqlQuery("SELECT p.id" +
        "   FROM projects p" +
        "       WHERE EXISTS (" +
        "          SELECT 1" +
        "          FROM project_versions pv" +
        "          JOIN project_version_downloads pvf ON pv.id = pvf.version_id" +
        "          WHERE pv.project_id = p.id" +
        "            AND pvf.hash = :hash" +
        "   )" +
        "   <if(!canSeeHidden)> AND (p.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(p.project_members) AND p.visibility != 4)<endif>) <endif>")
    @Nullable Long getProjectIdFromVersionHash(String hash, @Define boolean canSeeHidden, @Define @Bind Long requesterId);

    @RegisterConstructorMapper(ProjectMember.class)
    @RegisterColumnMapperFactory(CompactRoleColumnMapperFactory.class)
    @SqlQuery("SELECT u.name AS \"user\", u.id AS \"userId\", array_agg(r.name) roles " +
        "   FROM user_project_roles upr" +
        "       JOIN users u ON upr.user_id = u.id " +
        "       JOIN roles r ON upr.role_type = r.name " +
        "   WHERE upr.project_id = :id " +
        "   GROUP BY u.name, u.id ORDER BY max(r.permission::bigint) DESC " +
        "   <offsetLimit>")
    List<ProjectMember> getProjectMembers(long id, @BindPagination RequestPagination pagination);

    @SqlQuery("SELECT count(*) " +
        "   FROM user_project_roles upr " +
        "       JOIN users u ON upr.user_id = u.id " +
        "   WHERE upr.project_id = :id " +
        "   GROUP BY u.name")
    long getProjectMembersCount(long id);

    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.locked," +
        "       u.socials, " +
        "       u.avatar_url," +
        "       array_agg(r.id) AS roles," +
        "       (SELECT count(*)" +
        "           FROM project_members_all pma" +
        "           WHERE pma.user_id = u.id" +
        "       ) AS project_count" +
        "   FROM project_stars ps " +
        "       JOIN users u ON ps.user_id = u.id " +
        "       LEFT JOIN user_global_roles ugr ON u.id = ugr.user_id" +
        "       LEFT JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE ps.project_id = :id " +
        "   GROUP BY u.id" +
        "   LIMIT :limit OFFSET :offset")
    List<User> getProjectStargazers(long id, long limit, long offset);

    @SqlQuery("SELECT count(ps.user_id) " +
        "   FROM project_stars ps " +
        "   WHERE ps.project_id = :id " +
        "   GROUP BY ps.user_id")
    Long getProjectStargazersCount(long id);

    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.locked," +
        "       u.socials, " +
        "       u.avatar_url," +
        "       array_agg(r.id) AS roles," +
        "       (SELECT count(*)" +
        "           FROM project_members_all pma" +
        "           WHERE pma.user_id = u.id" +
        "       ) AS project_count" +
        "   FROM project_watchers pw " +
        "       JOIN users u ON pw.user_id = u.id " +
        "       LEFT JOIN user_global_roles ugr ON u.id = ugr.user_id" +
        "       LEFT JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE pw.project_id = :id" +
        "   GROUP BY u.id" +
        "   LIMIT :limit OFFSET :offset")
    List<User> getProjectWatchers(long id, long limit, long offset);

    @SqlQuery("SELECT count(pw.user_id) " +
        "   FROM project_watchers pw " +
        "   WHERE pw.project_id = :id " +
        "   GROUP BY pw.user_id")
    Long getProjectWatchersCount(long id);

    @KeyColumn("dateKey")
    @RegisterConstructorMapper(DayProjectStats.class)
    @SqlQuery("SELECT cast(dates.day AS date) datekey, coalesce(sum(pvd.downloads), 0) AS downloads, coalesce(pv.views, 0) AS views" +
        "   FROM (SELECT generate_series(:fromDate::date, :toDate::date, INTERVAL '1 DAY') AS day) dates " +
        "       LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day" +
        "       LEFT JOIN project_views pv ON dates.day = pv.day AND pvd.project_id = pv.project_id" +
        "   WHERE " +
        "       (pvd IS NULL OR pvd.project_id = :id)" +
        "   GROUP BY pv.views, dates.day")
    Map<String, DayProjectStats> getProjectStats(long id, OffsetDateTime fromDate, OffsetDateTime toDate);
}
