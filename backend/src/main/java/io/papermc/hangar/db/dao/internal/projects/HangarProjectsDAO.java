package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.db.mappers.factories.JoinableRowMapperFactory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

@JdbiRepository
public interface HangarProjectsDAO {

    @RegisterConstructorMapper(Project.class)
    @SqlQuery("""
        SELECT p.id,
               p.created_at,
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
               hp.avatar,
               hp.avatar_fallback,
               hp.supported_platforms
          FROM home_projects hp
                 JOIN projects_extra p ON hp.id = p.id
                 JOIN users u ON p.owner_id = u.id
                 WHERE p.id = :projectId""")
    Project getProject(long projectId, Long currentUserId);

    @RegisterRowMapperFactory(JoinableRowMapperFactory.class)
    @RegisterConstructorMapper(UserTable.class)
    @RegisterConstructorMapper(value = ProjectRoleTable.class, prefix = "upr_")
    @UseStringTemplateEngine
    @SqlQuery("SELECT u.*," +
        "       upr.id upr_id," +
        "       upr.created_at upr_created_at," +
        "       upr.user_id upr_user_id," +
        "       upr.role_type upr_role_type," +
        "       upr.project_id upr_project_id," +
        "       upr.accepted upr_accepted" +
        "   FROM user_project_roles upr" +
        "       JOIN users u ON upr.user_id = u.id" +
        "   WHERE upr.project_id = :projectId <if(!canSeePending)>AND (upr.accepted IS TRUE OR upr.user_id = :userId)<endif>")
    List<JoinableMember<ProjectRoleTable>> getProjectMembers(long projectId, Long userId, @Define boolean canSeePending);

    @RegisterConstructorMapper(HangarProject.HangarProjectInfo.class)
    @SqlQuery("""
        SELECT (SELECT count(DISTINCT pv.id) from project_versions pv where p.id = pv.project_id and pv.visibility = 0)  public_versions,
               (SELECT count(DISTINCT pf.id) from project_flags pf where p.id = pf.project_id)                           flag_count,
               (SELECT count(DISTINCT ps.user_id) from project_stars ps where p.id = ps.project_id)                      star_count,
               (SELECT count(DISTINCT pw.user_id) from project_watchers pw where p.id = pw.project_id)                   watcher_count,
               (SELECT count(DISTINCT pn.id) from project_notes pn where p.id = pn.project_id)                           note_count
        FROM projects p
        WHERE p.id = :projectId
    """)
    HangarProject.HangarProjectInfo getHangarProjectInfo(long projectId);

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
