package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.db.mappers.factories.JoinableRowMapperFactory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.ProjectData;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.jspecify.annotations.Nullable;

@JdbiRepository
public interface HangarProjectsDAO {

    @RegisterConstructorMapper(Project.class)
    @RegisterConstructorMapper(ProjectData.class)
    @SqlQuery("""
        SELECT p.id,
               p.created_at,
               p.published_at,
               p.name,
               p.owner_name AS owner,
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
               exists(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS starred,
               exists(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS watching,
               exists(SELECT * FROM project_flags pf WHERE pf.project_id = p.id AND pf.user_id = :currentUserId AND pf.resolved IS FALSE) AS flagged,
               p.links,
               p.license_name,
               p.license_type,
               p.license_url,
               p.tags,
               p.keywords,
               p.donation_enabled,
               p.donation_subject,
               p.sponsors,
               p.unlisted,
               hp.avatar,
               hp.avatar_fallback,
               hp.supported_platforms,
               (SELECT count(DISTINCT pv.id) FROM project_versions pv WHERE p.id = pv.project_id AND pv.visibility = 0) AS public_versions,
               (SELECT count(DISTINCT pf.id) FROM project_flags pf WHERE p.id = pf.project_id)                          AS flag_count,
               (SELECT count(DISTINCT ps.user_id) FROM project_stars ps WHERE p.id = ps.project_id)                     AS star_count,
               (SELECT count(DISTINCT pw.user_id) FROM project_watchers pw WHERE p.id = pw.project_id)                  AS watcher_count,
               (SELECT count(DISTINCT pn.id) FROM project_notes pn WHERE p.id = pn.project_id)                          AS note_count
          FROM projects_extra p
              LEFT JOIN home_projects hp ON hp.id = p.id
                 JOIN users u ON p.owner_id = u.id
                 WHERE p.id = :projectId""")
    ProjectData getProject(long projectId, @Nullable Long currentUserId);

    @RegisterRowMapperFactory(JoinableRowMapperFactory.class)
    @RegisterConstructorMapper(UserTable.class)
    @RegisterConstructorMapper(value = ProjectRoleTable.class, prefix = "upr_")
    @UseStringTemplateEngine
    @SqlQuery("SELECT u.*," +
        "       upr.id upr_id," +
        "       upr.created_at upr_created_at," +
        "       upr.user_id upr_user_id," +
        "       upr.permissions::bigint upr_permissions," +
        "       upr.title upr_title," +
        "       upr.is_owner upr_is_owner," +
        "       upr.project_id upr_project_id," +
        "       upr.accepted upr_accepted" +
        "   FROM user_project_roles upr" +
        "       JOIN users u ON upr.user_id = u.id" +
        "   WHERE upr.project_id = :projectId <if(!canSeePending)>AND (upr.accepted IS TRUE OR upr.user_id = :userId)<endif>")
    List<JoinableMember<ProjectRoleTable>> getProjectMembers(long projectId, @Nullable Long userId, @Define boolean canSeePending);

    @RegisterConstructorMapper(HangarChannel.class)
    @SqlQuery("SELECT pc.*," +
        "   (SELECT count(*) FROM project_versions pv WHERE pv.channel_id = pc.id) AS version_count" +
        "   FROM project_channels pc" +
        "   WHERE pc.id = :channelId")
    HangarChannel getHangarChannel(long channelId);

    @RegisterConstructorMapper(HangarChannel.class)
    @SqlQuery("SELECT pc.*," +
        "   (SELECT count(*) FROM project_versions pv WHERE pv.channel_id = pc.id) AS version_count" +
        "   FROM project_channels pc" +
        "   WHERE pc.project_id = :projectId" +
        "   ORDER BY pc.created_at")
    List<HangarChannel> getHangarChannels(long projectId);
}
