package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.model.internal.projects.HangarProjectApproval;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarProjectApproval.class)
public interface HangarProjectsAdminDAO {

    @SqlQuery("SELECT sq.id project_id," +
        "       sq.owner_name pn_owner," +
        "       sq.slug pn_slug," +
        "       sq.visibility visibility," +
        "       sq.last_comment \"comment\"," +
        "       u.name change_requester" +
        "  FROM (SELECT p.id," +
        "               p.owner_name," +
        "               p.slug," +
        "               p.visibility," +
        "               vc.resolved_at," +
        "               lag(vc.comment) OVER last_vc    AS last_comment," +
        "               lag(vc.visibility) OVER last_vc AS last_visibility," +
        "               lag(vc.created_by) OVER last_vc AS last_changer" +
        "          FROM projects p" +
        "                 JOIN project_visibility_changes vc ON p.id = vc.project_id" +
        "          WHERE p.visibility = 3 WINDOW last_vc AS (PARTITION BY p.id ORDER BY vc.created_at)) sq" +
        "         JOIN users u ON sq.last_changer = u.id" +
        "  WHERE sq.resolved_at IS NULL" +
        "    AND sq.last_visibility = 2" +
        "  ORDER BY sq.owner_name || sq.slug")
    List<HangarProjectApproval> getVisibilityNeedsApproval();

    @SqlQuery("""
        SELECT count(sq.id)
          FROM (SELECT p.id,
                       vc.resolved_at,
                       lag(vc.visibility) OVER last_vc AS last_visibility
                  FROM projects p
                         JOIN project_visibility_changes vc ON p.id = vc.project_id
                  WHERE p.visibility = 3 WINDOW last_vc AS (PARTITION BY p.id ORDER BY vc.created_at)) sq
          WHERE sq.resolved_at IS NULL
            AND sq.last_visibility = 2
        """)
    int getProjectsNeedingApprovalSize();

    @SqlQuery("SELECT p.id project_id, p.owner_name pn_owner, p.slug pn_slug, p.visibility visibility, vc.comment \"comment\", u.name change_requester" +
        "  FROM projects p" +
        "         JOIN project_visibility_changes vc ON p.id = vc.project_id" +
        "         JOIN users u ON vc.created_by = u.id" +
        "  WHERE vc.resolved_at IS NULL" +
        "    AND p.visibility = 2")
    List<HangarProjectApproval> getVisibilityWaitingProject();

}
