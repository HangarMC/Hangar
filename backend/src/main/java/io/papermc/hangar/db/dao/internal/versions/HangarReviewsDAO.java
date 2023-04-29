package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.model.internal.versions.HangarReviewQueueEntry;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarReview.class)
public interface HangarReviewsDAO {

    @UseRowReducer(HangarReviewReducer.class)
    @RegisterConstructorMapper(value = HangarReview.HangarReviewMessage.class, prefix = "hrm_")
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

    class HangarReviewReducer implements LinkedHashMapRowReducer<Long, HangarReview> {
        @Override
        public void accumulate(final Map<Long, HangarReview> container, final RowView rowView) {
            final HangarReview hangarReview = container.computeIfAbsent(rowView.getColumn("id", Long.class), id -> rowView.getRow(HangarReview.class));

            if (rowView.getColumn("hrm_created_at", OffsetDateTime.class) != null) {
                hangarReview.getMessages().add(rowView.getRow(HangarReview.HangarReviewMessage.class));
            }
        }
    }

    @UseRowReducer(ReviewQueueReducer.class)
    @RegisterConstructorMapper(HangarReviewQueueEntry.class)
    @RegisterConstructorMapper(value = HangarReviewQueueEntry.Review.class, prefix = "r_")
    @SqlQuery("SELECT pv.id AS version_id," +
        "       p.owner_name pn_owner," +
        "       p.slug pn_slug," +
        "       pv.version_string," +
        "       array(SELECT DISTINCT plv.platform " +
        "               FROM project_version_platform_dependencies pvpd " +
        "                   JOIN platform_versions plv ON pvpd.platform_version_id = plv.id" +
        "               WHERE pv.id = pvpd.version_id" +
        "               ORDER BY plv.platform" +
        "       ) platforms," +
        "       pv.created_at version_created_at," +
        "       coalesce(pvu.name, 'DELETED USER') version_author," +
        "       pc.name channel_name," +
        "       pc.description channel_description," +
        "       pc.color channel_color," +
        "       ru.name r_reviewer_name," +
        "       pvr.created_at r_review_started," +
        "       pvr.ended_at r_review_ended," +
        "       (SELECT pvrm.action FROM project_version_review_messages pvrm WHERE pvr.id = pvrm.review_id ORDER BY pvrm.created_at DESC LIMIT 1) r_last_action" +
        "   FROM project_versions pv" +
        "       LEFT JOIN project_version_reviews pvr ON pv.id = pvr.version_id" +
        "       LEFT JOIN users ru ON pvr.user_id = ru.id" +
        "       LEFT JOIN users pvu ON pv.author_id = pvu.id" +
        "       JOIN project_channels pc ON pv.channel_id = pc.id" +
        "       JOIN projects p ON pv.project_id = p.id" +
        "   WHERE pv.review_state = :reviewState AND" +
        "         p.visibility != 4 AND" +
        "         pv.visibility != 4" +
        "   GROUP BY pv.id, p.id, pvu.id, pc.id, pvr.id, ru.id" +
        "   ORDER BY pv.created_at")
    List<HangarReviewQueueEntry> getReviewQueue(@EnumByOrdinal ReviewState reviewState);

    @SqlQuery("""
        SELECT count(pv.id)
           FROM project_versions pv
               JOIN projects p ON pv.project_id = p.id
           WHERE pv.review_state = :reviewState AND
                 p.visibility != 4 AND
                 pv.visibility != 4
        """)
    int getReviewQueueSize(@EnumByOrdinal ReviewState reviewState);

    class ReviewQueueReducer implements LinkedHashMapRowReducer<Long, HangarReviewQueueEntry> {
        @Override
        public void accumulate(final Map<Long, HangarReviewQueueEntry> container, final RowView rowView) {
            final HangarReviewQueueEntry entry = container.computeIfAbsent(rowView.getColumn("version_id", Long.class), id -> rowView.getRow(HangarReviewQueueEntry.class));

            if (rowView.getColumn("r_reviewer_name", String.class) != null) {
                entry.getReviews().add(rowView.getRow(HangarReviewQueueEntry.Review.class));
            }
        }
    }
}
