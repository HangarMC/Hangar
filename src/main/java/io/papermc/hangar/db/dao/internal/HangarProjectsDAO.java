package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.db.mappers.factories.JoinableMemberFactory;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.internal.HangarProject;
import io.papermc.hangar.model.internal.HangarProjectFlag;
import io.papermc.hangar.model.internal.user.JoinableMember;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
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
            "       ps.forum_sync" +
            "  FROM home_projects p" +
            "         JOIN projects ps ON p.id = ps.id" +
            "         JOIN users u ON ps.owner_id = u.id" +
            "         WHERE p.slug = :slug AND p.owner_name = :author")
    Pair<Long, Project> getProject(String author, String slug, Long currentUserId);

    @RegisterRowMapperFactory(JoinableMemberFactory.class)
    @RegisterConstructorMapper(UserTable.class)
    @RegisterConstructorMapper(value = ProjectRoleTable.class, prefix = "upr_")
    @SqlQuery("SELECT u.*," +
            "       upr.id upr_id," +
            "       upr.created_at upr_created_at," +
            "       upr.user_id upr_user_id," +
            "       upr.role_type upr_role_type," +
            "       upr.project_id upr_project_id," +
            "       upr.accepted upr_accepted" +
            "   FROM user_project_roles upr" +
            "       JOIN users u ON upr.user_id = u.id" +
            "   WHERE upr.project_id = :projectId")
    List<JoinableMember<ProjectRoleTable>> getProjectMembers(long projectId);

    @RegisterConstructorMapper(HangarProject.HangarProjectInfo.class)
    @SqlQuery("SELECT count(pv.*) public_versions," +
            "       count(ps.*) star_count," +
            "       count(pw.*) watcher_count," +
            "       coalesce(jsonb_array_length(p.notes->'messages'), 0) note_count" +
            "   FROM projects p" +
            "       LEFT JOIN project_versions pv ON p.id = pv.project_id AND pv.visibility = 0" +
            "       LEFT JOIN project_stars ps ON p.id = ps.project_id" +
            "       LEFT JOIN project_watchers pw ON p.id = pw.project_id" +
            "   WHERE p.id = :projectId" +
            "   GROUP BY p.id")
    HangarProject.HangarProjectInfo getHangarProjectInfo(long projectId);

    @RegisterConstructorMapper(HangarProjectFlag.class)
    @SqlQuery("SELECT pf.*, " +
            "       u.name reported_by_name," +
            "       u2.name resolved_by_name," +
            "       p.owner_name project_owner_name," +
            "       p.slug project_slug," +
            "       p.visibility project_visibility" +
            "   FROM project_flags pf" +
            "       JOIN users u ON pf.user_id = u.id" +
            "       LEFT JOIN users u2 ON pf.resolved_by = u2.id" +
            "       JOIN projects p ON pf.project_id = p.id" +
            "   WHERE lower(p.owner_name) = lower(:author) AND lower(p.slug) = lower(:slug)")
    List<HangarProjectFlag> getHangarProjectFlags(String author, String slug);

    @SqlUpdate("REFRESH MATERIALIZED VIEW home_projects")
    void refreshHomeProjects();
}
