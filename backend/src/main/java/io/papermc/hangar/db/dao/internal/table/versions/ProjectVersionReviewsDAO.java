package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewMessageTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewTable;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionReviewTable.class)
@RegisterConstructorMapper(ProjectVersionReviewMessageTable.class)
public interface ProjectVersionReviewsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_reviews (created_at, version_id, user_id, ended_at) VALUES (:now, :versionId, :userId, :endedAt)")
    ProjectVersionReviewTable insert(@BindBean ProjectVersionReviewTable projectVersionReviewTable);

    @Timestamped
    @SqlUpdate("INSERT INTO project_version_review_messages (created_at, review_id, message, args, action) VALUES (:now, :reviewId, :message, :args, :action)")
    void insertMessage(@BindBean ProjectVersionReviewMessageTable projectVersionReviewMessageTable);

    @SqlUpdate("UPDATE project_version_reviews SET ended_at = :endedAt WHERE id = :id")
    void update(@BindBean ProjectVersionReviewTable projectVersionReviewTable);

    @SqlQuery("SELECT * FROM project_version_reviews WHERE version_id = :versionId AND user_id = :userId")
    ProjectVersionReviewTable getUsersReview(long versionId, long userId);

    @SqlQuery("SELECT * FROM project_version_reviews WHERE version_id = :versionId AND user_id = :userId AND ended_at IS NULL ORDER BY created_at DESC LIMIT 1")
    ProjectVersionReviewTable getLatestUnfinishedReview(long versionId, long userId);

    @SqlQuery("SELECT * FROM project_version_reviews WHERE version_id = :versionId AND ended_at IS NULL ORDER BY created_at DESC")
    List<ProjectVersionReviewTable> getUnfinishedReviews(long versionId);

    @SqlQuery("SELECT pvrm.* " +
        "   FROM project_version_review_messages pvrm" +
        "       JOIN project_version_reviews pvr ON pvrm.review_id = pvr.id" +
        "   WHERE pvr.version_id = :versionId AND pvr.user_id = :userId" +
        "   ORDER BY pvrm.created_at DESC LIMIT 1")
    ProjectVersionReviewMessageTable getLatestMessage(long versionId, long userId);
}
