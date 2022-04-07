package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.DayProjectStats;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.ProjectMember;
import io.papermc.hangar.model.api.requests.RequestPagination;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.DefineNamedBindings;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RegisterConstructorMapper(Project.class)
public interface ProjectsApiDAO {

    @UseStringTemplateEngine
    @RegisterColumnMapper(PromotedVersionMapper.class)
    @SqlQuery("SELECT hp.id," +
        "       hp.created_at," +
        "       hp.name," +
        "       hp.owner_name \"owner\"," +
        "       hp.slug," +
        "       hp.promoted_versions," +
        "       hp.views," +
        "       hp.downloads," +
        "       hp.recent_views," +
        "       hp.recent_downloads," +
        "       hp.stars," +
        "       hp.watchers," +
        "       hp.category," +
        "       hp.description," +
        "       COALESCE(hp.last_updated, hp.created_at) AS last_updated," +
        "       hp.visibility, " +
        "       exists(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS starred, " +
        "       exists(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS watching, " +
        "       exists(SELECT * FROM project_flags pf WHERE pf.project_id = p.id AND pf.user_id = :requesterId AND pf.resolved IS FALSE) as flagged," +
        "       p.homepage," +
        "       p.issues," +
        "       p.source," +
        "       p.support," +
        "       p.license_name," +
        "       p.license_url," +
        "       p.keywords," +
        "       p.forum_sync," +
        "       p.topic_id," +
        "       p.post_id," +
        "       p.donation_enabled," +
        "       p.donation_subject," +
        "       p.sponsors" +
        "  FROM home_projects hp" +
        "         JOIN projects p ON hp.id = p.id" +
        "         WHERE lower(hp.slug) = lower(:slug) AND" +
        "           lower(hp.owner_name) = lower(:author)" +
        "         <if(!canSeeHidden)> AND (hp.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(hp.project_members) AND hp.visibility != 4)<endif>) <endif>")
    Project getProject(String author, String slug, @Define boolean canSeeHidden, @Define @Bind Long requesterId);

    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT DISTINCT (hp.id),
            hp.created_at,
            hp.name,
            hp.owner_name "owner",
            hp.slug,
            hp.promoted_versions,
            hp.views,
            hp.downloads,
            hp.recent_views,
            hp.recent_downloads,
            hp.stars,
            hp.watchers,
            hp.category,
            hp.description,
            hp.last_updated AS dum, --- need to add this so we can use it in order by, special constraint on distinct queries
            COALESCE(hp.last_updated, hp.created_at) AS last_updated,
            ((EXTRACT(EPOCH FROM COALESCE(hp.last_updated, hp.created_at)) - 1609459200) / 604800) *1 AS last_updated_double, --- We can order with this. That "dum" does not work. It only orders it with this.
            hp.visibility,
            exists(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS starred,
            exists(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :requesterId) AS watching,
            exists(SELECT * FROM project_flags pf WHERE pf.project_id = p.id AND pf.user_id = :requesterId AND pf.resolved IS FALSE) as flagged,
            p.homepage,
            p.issues,
            p.source,
            p.support,
            p.license_name,
            p.license_url,
            p.keywords,
            p.forum_sync,
            p.topic_id,
            p.post_id,
            p.donation_enabled,
            p.donation_subject,
            p.sponsors
        FROM home_projects hp
            JOIN projects p on hp.id = p.id
            LEFT JOIN project_versions pv on p.id = pv.project_id
            LEFT JOIN project_version_platform_dependencies pvpd on pv.id = pvpd.version_id
            LEFT JOIN platform_versions v on pvpd.platform_version_id = v.id
            WHERE true <filters> -- Not sure how else to get here a single Where
            <if(!seeHidden)> AND (hp.visibility = 0 <if(requesterId)>OR (:requesterId = ANY(hp.project_members) AND hp.visibility != 4)<endif>) <endif>
            ORDER BY <orderBy>
            <offsetLimit>""")
    @RegisterColumnMapper(PromotedVersionMapper.class)
    @DefineNamedBindings
    List<Project> getProjects(@Define boolean seeHidden, Long requesterId, @Define String orderBy,
                              @BindPagination RequestPagination pagination);

    // This query can be shorter because it doesnt need all those column values as above does, just a single column for the amount of rows to be counted
    @UseStringTemplateEngine
    @SqlQuery("SELECT count(DISTINCT hp.id) " +
        "  FROM home_projects hp" +
        "         JOIN projects p ON hp.id = p.id" +
        "         LEFT JOIN project_versions pv on p.id = pv.project_id " +
        "         LEFT JOIN project_version_platform_dependencies pvpd on pv.id = pvpd.version_id " +
        "         LEFT JOIN platform_versions v on pvpd.platform_version_id = v.id " +
        "         WHERE true <filters>" + // Not sure how else to get here a single Where
        "         <if(!seeHidden)> AND (hp.visibility = 0 <if(requesterId)>OR (<requesterId> = ANY(hp.project_members) AND hp.visibility != 4)<endif>) <endif> ")
    long countProjects(@Define boolean seeHidden, @Define Long requesterId,
                       @BindPagination(isCount = true) RequestPagination pagination);

    @RegisterConstructorMapper(ProjectMember.class)
    @SqlQuery("SELECT u.name AS \"user\", array_agg(r.name) roles " +
        "   FROM projects p " +
        "       JOIN user_project_roles upr ON p.id = upr.project_id " +
        "       JOIN users u ON upr.user_id = u.id " +
        "       JOIN roles r ON upr.role_type = r.name " +
        "   WHERE p.slug = :slug AND p.owner_name = :author " +
        "   GROUP BY u.name ORDER BY max(r.permission::BIGINT) DESC " +
        "   <offsetLimit>")
    List<ProjectMember> getProjectMembers(String author, String slug, @BindPagination RequestPagination pagination);

    @SqlQuery("SELECT count(*) " +
        "   FROM projects p " +
        "       JOIN user_project_roles upr ON p.id = upr.project_id " +
        "       JOIN users u ON upr.user_id = u.id " +
        "   WHERE p.slug = :slug AND p.owner_name = :author " +
        "   GROUP BY u.name")
    long getProjectMembersCount(String author, String slug);

    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.join_date," +
        "       u.locked," +
        "       array_agg(r.name) AS roles," +
        "       (SELECT count(*)" +
        "           FROM project_members_all pma" +
        "           WHERE pma.user_id = u.id" +
        "       ) AS project_count" +
        "   FROM projects p " +
        "       JOIN project_stars ps ON p.id = ps.project_id " +
        "       JOIN users u ON ps.user_id = u.id " +
        "       LEFT JOIN user_global_roles ugr ON u.id = ugr.user_id" +
        "       LEFT JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE p.slug = :slug AND p.owner_name = :author " +
        "   GROUP BY u.id" +
        "   LIMIT :limit OFFSET :offset")
    List<User> getProjectStargazers(String author, String slug, long limit, long offset);

    @SqlQuery("SELECT count(ps.user_id) " +
        "   FROM projects p " +
        "       JOIN project_stars ps ON p.id = ps.project_id " +
        "   WHERE p.slug = :slug AND p.owner_name = :author " +
        "   GROUP BY ps.user_id")
    Long getProjectStargazersCount(String author, String slug);

    @RegisterConstructorMapper(User.class)
    @SqlQuery("SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       u.join_date," +
        "       u.locked," +
        "       array_agg(r.name) AS roles," +
        "       (SELECT count(*)" +
        "           FROM project_members_all pma" +
        "           WHERE pma.user_id = u.id" +
        "       ) AS project_count" +
        "   FROM projects p " +
        "       JOIN project_watchers pw ON p.id = pw.project_id " +
        "       JOIN users u ON pw.user_id = u.id " +
        "       LEFT JOIN user_global_roles ugr ON u.id = ugr.user_id" +
        "       LEFT JOIN roles r ON ugr.role_id = r.id" +
        "   WHERE p.slug = :slug AND p.owner_name = :author" +
        "   GROUP BY u.id" +
        "   LIMIT :limit OFFSET :offset")
    List<User> getProjectWatchers(String author, String slug, long limit, long offset);

    @SqlQuery("SELECT count(pw.user_id) " +
        "   FROM projects p " +
        "       JOIN project_watchers pw ON p.id = pw.project_id " +
        "   WHERE p.slug = :slug AND p.owner_name = :author " +
        "   GROUP BY pw.user_id")
    Long getProjectWatchersCount(String author, String slug);

    @KeyColumn("dateKey")
    @RegisterConstructorMapper(DayProjectStats.class)
    @SqlQuery("SELECT cast(dates.day AS date) dateKey, coalesce(sum(pvd.downloads), 0) AS downloads, coalesce(pv.views, 0) AS views" +
        "   FROM projects p," +
        "   (SELECT generate_series(:fromDate::date, :toDate::date, INTERVAL '1 DAY') as day) dates " +
        "       LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day" +
        "       LEFT JOIN project_views pv ON dates.day = pv.day AND pvd.project_id = pv.project_id" +
        "   WHERE " +
        "       p.owner_name = :author AND " +
        "       p.slug = :slug AND" +
        "       (pvd IS NULL OR pvd.project_id = p.id)" +
        "   GROUP BY pv.views, dates.day")
    Map<String, DayProjectStats> getProjectStats(String author, String slug, OffsetDateTime fromDate, OffsetDateTime toDate);
}
