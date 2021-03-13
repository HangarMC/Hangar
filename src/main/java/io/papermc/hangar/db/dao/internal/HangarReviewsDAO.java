package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.model.internal.versions.HangarReview.HangarReviewMessage;
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
