package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.db.mappers.factories.JoinableRowMapperFactory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.projects.HangarChannel;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.user.JoinableMember;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
public interface HangarProjectsDAO {

    @RegisterConstructorMapper(Project.class)
    @SqlQuery("SELECT p.id," +
        "       p.created_at," +
        "       p.name," +
        "       p.owner_name AS owner," +
        "       p.slug," +
        "       hp.views," +
        "       hp.downloads," +
        "       hp.recent_views," +
        "       hp.recent_downloads," +
        "       p.stars," +
        "       p.watchers," +
        "       p.category," +
        "       p.description," +
        "       coalesce(p.last_updated, p.created_at) AS last_updated," +
        "       p.visibility, " +
        "       exists(SELECT * FROM project_stars s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS starred," +
        "       exists(SELECT * FROM project_watchers s WHERE s.project_id = p.id AND s.user_id = :currentUserId) AS watching," +
        "       exists(SELECT * FROM project_flags pf WHERE pf.project_id = p.id AND pf.user_id = :currentUserId AND pf.resolved IS FALSE) AS flagged," +
        "       p.links," +
        "       p.license_name," +
        "       p.license_type," +
        "       p.license_url," +
        "       p.tags," +
        "       p.keywords," +
        "       p.donation_enabled," +
        "       p.donation_subject," +
        "       p.sponsors" +
        "  FROM home_projects hp" +
        "         JOIN projects_extra p ON hp.id = p.id" +
        "         JOIN users u ON p.owner_id = u.id" +
        "         WHERE lower(p.slug) = lower(:slug)")
    Project getProject(String slug, Long currentUserId);

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

    @SqlUpdate("REFRESH MATERIALIZED VIEW home_projects")
    void refreshHomeProjects();
}
