package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.mappers.PromotedVersionMapper;
import io.papermc.hangar.model.api.project.ProjectCompact;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(ProjectCompact.class)
@RegisterColumnMapper(PromotedVersionMapper.class)
@UseStringTemplateEngine
public interface UsersApiDAO {

    @SqlQuery("SELECT p.created_at," +
            " p.name," +
            " p.owner_name \"owner\"," +
            " p.slug," +
            " p.promoted_versions," +
            " p.views," +
            " p.downloads," +
            " p.recent_views," +
            " p.recent_downloads," +
            " p.stars," +
            " p.watchers," +
            " p.category," +
            " p.visibility" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects p ON ps.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user" +
            " ORDER BY :sortOrder LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserStarred(String user, @Define boolean canSeeHidden, @Define Long userId, String sortOrder, long limit, long offset);

    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_stars ps ON u.id = ps.user_id" +
            "     JOIN home_projects p ON ps.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserStarredCount(String user, @Define boolean canSeeHidden, @Define Long userId);

    @SqlQuery("SELECT p.created_at," +
            " p.name," +
            " p.owner_name \"owner\"," +
            " p.slug," +
            " p.promoted_versions," +
            " p.views," +
            " p.downloads," +
            " p.recent_views," +
            " p.recent_downloads," +
            " p.stars," +
            " p.watchers," +
            " p.category," +
            " p.visibility" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects p ON pw.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user" +
            " ORDER BY :sortOrder LIMIT :limit OFFSET :offset")
    List<ProjectCompact> getUserWatching(String user, @Define boolean canSeeHidden, @Define Long userId, String sortOrder, long limit, long offset);

    @SqlQuery("SELECT count(*)" +
            " FROM users u " +
            "     JOIN project_watchers pw ON u.id = pw.user_id" +
            "     JOIN home_projects p ON pw.project_id = p.id" +
            " WHERE " +
            "     <if(!canSeeHidden)> (p.visibility = 0 OR p.visibility = 1" +
            "     <if(userId)>OR (<userId> = ANY(p.project_members) AND p.visibility != 4)<endif>) AND<endif>" +
            "     u.name = :user")
    long getUserWatchingCount(String user, @Define boolean canSeeHidden, @Define Long userId);
}
