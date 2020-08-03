package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectVersionsTable;
import me.minidigger.hangar.model.generated.ReviewState;
import me.minidigger.hangar.model.viewhelpers.ReviewQueueEntry;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectVersionsTable.class)
public interface ProjectVersionDao {

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId")
    ProjectVersionsTable getVersion(long projectId);

    @RegisterBeanMapper(ReviewQueueEntry.class)
    @SqlQuery("SELECT sq.project_author," +
            "       sq.project_slug," +
            "       sq.project_name," +
            "       sq.version_string," +
            "       sq.version_created_at," +
            "       sq.channel_name," +
            "       sq.channel_color," +
            "       sq.version_author," +
            "       sq.reviewer_id," +
            "       sq.reviewer_name," +
            "       sq.review_started," +
            "       sq.review_ended" +
            "  FROM (SELECT pu.name                                                                  AS project_author," +
            "               p.name                                                                   AS project_name," +
            "               p.slug                                                                   AS project_slug," +
            "               v.version_string," +
            "               v.created_at                                                             AS version_created_at," +
            "               c.name                                                                   AS channel_name," +
            "               c.color                                                                  AS channel_color," +
            "               vu.name                                                                  AS version_author," +
            "               r.user_id                                                                AS reviewer_id," +
            "               ru.name                                                                  AS reviewer_name," +
            "               r.created_at                                                             AS review_started," +
            "               r.ended_at                                                               AS review_ended," +
            "               row_number() OVER (PARTITION BY (p.id, v.id) ORDER BY r.created_at DESC) AS row" +
            "          FROM project_versions v" +
            "                 LEFT JOIN users vu ON v.author_id = vu.id" +
            "                 INNER JOIN project_channels c ON v.channel_id = c.id" +
            "                 INNER JOIN projects p ON v.project_id = p.id" +
            "                 INNER JOIN users pu ON p.owner_id = pu.id" +
            "                 LEFT JOIN project_version_reviews r ON v.id = r.version_id" +
            "                 LEFT JOIN users ru ON ru.id = r.user_id" +
            "          WHERE v.review_state = :reviewState" +
            "            AND p.visibility != 5" +
            "            AND v.visibility != 5) sq" +
            "  WHERE row = 1" +
            "  ORDER BY sq.project_name DESC, sq.version_string DESC")
    List<ReviewQueueEntry> getQueue(@EnumByOrdinal ReviewState reviewState);
}
