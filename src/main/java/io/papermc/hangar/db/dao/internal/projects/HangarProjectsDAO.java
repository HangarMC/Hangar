package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.db.mappers.factories.JoinableMemberFactory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import io.papermc.hangar.model.internal.projects.HangarProject.HangarProjectInfo;
import io.papermc.hangar.model.internal.user.JoinableMember;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HangarProjectsDAO {

    @RegisterConstructorMapper(Project.class)
    @RegisterColumnMapper(PromotedVersionMapper.class)
    @SqlQuery("SELECT p.id," +
            "       p.created_at," +
            "       p.name," +
            "       p.owner_name AS owner," +
            "       p.slug," +
            "       p.promoted_versions," +
            "       p.views," +
            "       p.downloads," +
            "       p.recent_views," +
            "       p.recent_downloads," +
            "       p.stars," +
            "       p.watchers," +
            "       p.category," +
            "       p.description," +
            "       coalesce(p.last_updated, p.created_at) AS last_updated," +
            "       p.visibility, " +
            "       exists(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS starred, " +
            "       exists(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS watching, " +
            "       ps.homepage," +
            "       ps.issues," +
            "       ps.source," +
            "       ps.support," +
            "       ps.license_name," +
            "       ps.license_url," +
            "       ps.keywords," +
            "       ps.forum_sync" +
            "  FROM home_projects p" +
            "         JOIN projects ps ON p.id = ps.id" +
            "         JOIN users u ON ps.owner_id = u.id" +
            "         WHERE lower(p.slug) = lower(:slug) AND lower(p.owner_name) = lower(:author)")
    Pair<Long, Project> getProject(String author, String slug, Long currentUserId);

    @RegisterRowMapperFactory(JoinableMemberFactory.class)
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

    @RegisterConstructorMapper(HangarProjectInfo.class)
    @SqlQuery("SELECT count(DISTINCT pv.id) public_versions," +
            "       count(DISTINCT pf.id) flag_count," +
            "       count(DISTINCT ps.user_id) star_count," +
            "       count(DISTINCT pw.user_id) watcher_count," +
            "       count(DISTINCT pn.id) note_count" +
            "   FROM projects p" +
            "       LEFT JOIN project_versions pv ON p.id = pv.project_id AND pv.visibility = 0" +
            "       LEFT JOIN project_stars ps ON p.id = ps.project_id" +
            "       LEFT JOIN project_watchers pw ON p.id = pw.project_id" +
            "       LEFT JOIN project_flags pf ON p.id = pf.project_id" +
            "       LEFT JOIN project_notes pn ON p.id = pn.project_id" +
            "   WHERE p.id = :projectId" +
            "   GROUP BY p.id")
    HangarProjectInfo getHangarProjectInfo(long projectId);

    @RegisterConstructorMapper(HangarChannel.class)
    @SqlQuery("SELECT pc.*," +
            "   (SELECT count(*) FROM project_versions pv WHERE pv.channel_id = pc.id) as version_count" +
            "   FROM project_channels pc" +
            "   WHERE pc.id = :channelId")
    HangarChannel getHangarChannel(long channelId);

    @RegisterConstructorMapper(HangarChannel.class)
    @SqlQuery("SELECT pc.*," +
            "   (SELECT count(*) FROM project_versions pv WHERE pv.channel_id = pc.id) as version_count" +
            "   FROM project_channels pc" +
            "   WHERE pc.project_id = :projectId" +
            "   ORDER BY pc.created_at")
    List<HangarChannel> getHangarChannels(long projectId);

    @SqlUpdate("REFRESH MATERIALIZED VIEW home_projects")
    void refreshHomeProjects();
}
