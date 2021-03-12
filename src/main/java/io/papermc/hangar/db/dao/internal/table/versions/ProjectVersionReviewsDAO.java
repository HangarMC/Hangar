package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewMessageTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectVersionReviewTable.class)
@RegisterConstructorMapper(ProjectVersionReviewMessageTable.class)
public interface ProjectVersionReviewsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_reviews (created_at, version_id, user_id) VALUES (:now, :versionId, :userId)")
    ProjectVersionReviewTable insert(@BindBean ProjectVersionReviewTable projectVersionReviewTable);

    @Timestamped
    @SqlUpdate("INSERT INTO project_version_review_messages (created_at, review_id, message, action) VALUES (:now, :reviewId, :message, :action)")
    void insertMessage(@BindBean ProjectVersionReviewMessageTable projectVersionReviewMessageTable);
}
