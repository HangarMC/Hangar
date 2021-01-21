package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.modelold.ProjectVersionReviewsTable;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map.Entry;

@Repository
@RegisterBeanMapper(ProjectVersionReviewsTable.class)
public interface ProjectVersionReviewDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_version_reviews (created_at, version_id, user_id, comment) VALUES (:now, :versionId, :userId, :comment)")
    ProjectVersionReviewsTable insert(@BindBean ProjectVersionReviewsTable projectVersionReviewsTable);

    @SqlUpdate("UPDATE project_version_reviews SET ended_at = :endedAt, created_at = :createdAt, comment = :comment WHERE id = :id")
    void update(@BindBean ProjectVersionReviewsTable projectVersionReviewsTable);

    @KeyColumn("userName")
    @SqlQuery("SELECT pvr.*, u.name userName FROM project_version_reviews pvr JOIN users u ON pvr.user_id = u.id WHERE pvr.id = :id")
    Entry<String, ProjectVersionReviewsTable> getById(long id);

    @KeyColumn("userName")
    @SqlQuery("SELECT pvr.*, u.name userName " +
              "FROM project_version_reviews pvr" +
              "     JOIN users u ON pvr.user_id = u.id " +
              "WHERE pvr.version_id = :versionId ORDER BY pvr.created_at DESC")
    List<Entry<String, ProjectVersionReviewsTable>> getMostRecentReviews(long versionId);

    @KeyColumn("userName")
    @SqlQuery("SELECT pvr.*, u.name userName " +
            "FROM project_version_reviews pvr" +
            "     JOIN users u ON pvr.user_id = u.id " +
            "WHERE " +
            "   pvr.version_id = :versionId AND " +
            "   pvr.ended_at IS NULL " +
            "ORDER BY pvr.created_at")
    List<Entry<String, ProjectVersionReviewsTable>> getUnfinishedReviews(long versionId);
}
