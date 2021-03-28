package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.model.internal.versions.HangarReview.HangarReviewMessage;
import io.papermc.hangar.model.internal.versions.HangarReviewQueueEntry;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RegisterConstructorMapper(HangarReview.class)
public interface HangarReviewsDAO {

    @UseRowReducer(HangarReviewReducer.class)
    @RegisterConstructorMapper(value = HangarReviewMessage.class, prefix = "hrm_")
    @SqlQuery("SELECT pvr.id," +
            "       pvr.created_at," +
            "       pvr.ended_at," +
            "       u.name user_name," +
            "       pvr.user_id," +
            "       pvrm.created_at hrm_created_at," +
            "       pvrm.message hrm_message," +
            "       pvrm.args hrm_args," +
            "       pvrm.action hrm_action" +
            "   FROM project_version_reviews pvr" +
            "       JOIN users u ON pvr.user_id = u.id" +
            "       LEFT JOIN project_version_review_messages pvrm ON pvr.id = pvrm.review_id" +
            "   WHERE pvr.version_id = :versionId" +
            "   ORDER BY pvr.created_at DESC, pvrm.created_at")
    List<HangarReview> getReviews(long versionId);

    @RegisterConstructorMapper(HangarReviewQueueEntry.class)
    @SqlQuery("SELECT sq.project_author pn_owner," +
            "       sq.project_slug pn_slug," +
            "       sq.version_string," +
            "       sq.version_created_at," +
            "       sq.channel_name," +
            "       sq.channel_color," +
            "       sq.version_author," +
            "       sq.reviewer_name," +
            "       sq.review_started," +
            "       sq.review_ended" +
            "  FROM (SELECT p.owner_name                                                             AS project_author," +
            "               p.slug                                                                   AS project_slug," +
            "               v.version_string," +
            "               v.created_at                                                             AS version_created_at," +
            "               c.name                                                                   AS channel_name," +
            "               c.color                                                                  AS channel_color," +
            "               vu.name                                                                  AS version_author," +
            "               ru.name                                                                  AS reviewer_name," +
            "               r.created_at                                                             AS review_started," +
            "               r.ended_at                                                               AS review_ended," +
            "               row_number() OVER (PARTITION BY (p.id, v.id) ORDER BY r.created_at DESC) AS row" +
            "          FROM project_versions v" +
            "                 LEFT JOIN users vu ON v.author_id = vu.id" +
            "                 INNER JOIN project_channels c ON v.channel_id = c.id" +
            "                 INNER JOIN projects p ON v.project_id = p.id" +
            "                 LEFT JOIN project_version_reviews r ON v.id = r.version_id" +
            "                 LEFT JOIN users ru ON ru.id = r.user_id" +
            "          WHERE v.review_state = :reviewState" +
            "            AND p.visibility != 4" +
            "            AND v.visibility != 4) sq" +
            "  WHERE row = 1" +
            "  ORDER BY sq.review_started")
    List<HangarReviewQueueEntry> getReviewQueue(@EnumByOrdinal ReviewState reviewState);

    class HangarReviewReducer implements LinkedHashMapRowReducer<Long, HangarReview> {
        @Override
        public void accumulate(Map<Long, HangarReview> container, RowView rowView) {
            HangarReview hangarReview = container.computeIfAbsent(rowView.getColumn("id", Long.class), id -> rowView.getRow(HangarReview.class));

            if (rowView.getColumn("hrm_created_at", OffsetDateTime.class) != null) {
                hangarReview.getMessages().add(rowView.getRow(HangarReviewMessage.class));
            }
        }
    }

}
