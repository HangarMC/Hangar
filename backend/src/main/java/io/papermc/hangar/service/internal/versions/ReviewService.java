package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionReviewsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarReviewsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.ReviewAction;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewMessageTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewTable;
import io.papermc.hangar.model.internal.api.requests.versions.ReviewMessage;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.model.internal.versions.HangarReviewQueueEntry;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService extends HangarComponent {

    private final ProjectVersionReviewsDAO projectVersionReviewsDAO;
    private final HangarReviewsDAO hangarReviewsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final NotificationService notificationService;

    @Autowired
    public ReviewService(final ProjectVersionReviewsDAO projectVersionReviewsDAO, final HangarReviewsDAO hangarReviewsDAO, final ProjectVersionsDAO projectVersionsDAO, final ProjectVersionVisibilityService projectVersionVisibilityService, final NotificationService notificationService) {
        this.projectVersionReviewsDAO = projectVersionReviewsDAO;
        this.hangarReviewsDAO = hangarReviewsDAO;
        this.projectVersionsDAO = projectVersionsDAO;
        this.projectVersionVisibilityService = projectVersionVisibilityService;
        this.notificationService = notificationService;
    }

    public List<HangarReview> getHangarReviews(final long versionId) {
        return this.hangarReviewsDAO.getReviews(versionId);
    }

    @Transactional
    public void startReview(final long versionId, final ReviewMessage msg) {
        final ProjectVersionReviewTable possibleUserVersion = this.projectVersionReviewsDAO.getUsersReview(versionId, this.getHangarPrincipal().getUserId());
        if (possibleUserVersion != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.onlyOneReview");
        }
        final ProjectVersionReviewTable projectVersionReviewTable = this.projectVersionReviewsDAO.insert(new ProjectVersionReviewTable(versionId, this.getHangarPrincipal().getUserId()));
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg.getMessage(), new JSONB(msg.getArgs()), ReviewAction.START));
        this.changeVersionReviewState(versionId, ReviewState.UNDER_REVIEW, false);
    }

    @Transactional
    public void addReviewMessage(final long versionId, final ReviewMessage msg) {
        final ProjectVersionReviewTable latestUnfinishedReview = this.getLatestUnfinishedReviewAndValidate(versionId);
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.getMessage(), new JSONB(msg.getArgs()), ReviewAction.MESSAGE));
    }

    @Transactional
    public void stopReview(final long versionId, final ReviewMessage msg) {
        final ProjectVersionReviewTable latestUnfinishedReview = this.getLatestUnfinishedReviewAndValidate(versionId);
        latestUnfinishedReview.setEndedAt(OffsetDateTime.now());
        if (this.projectVersionReviewsDAO.getUnfinishedReviews(versionId).size() == 1) { // only unfinished is the one about to be finished
            this.changeVersionReviewState(versionId, ReviewState.UNREVIEWED, false);
        }
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.getMessage(), new JSONB(msg.getArgs()), ReviewAction.STOP));
        this.projectVersionReviewsDAO.update(latestUnfinishedReview);
    }

    @Transactional
    public void reopenReview(final long versionId, final ReviewMessage msg, final ReviewAction reviewAction) {
        final ProjectVersionReviewTable projectVersionReviewTable = this.projectVersionReviewsDAO.getUsersReview(versionId, this.getHangarPrincipal().getUserId());
        if (projectVersionReviewTable == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.noReviewStarted");
        }
        if (projectVersionReviewTable.getEndedAt() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.cannotReopen");
        }
        projectVersionReviewTable.setEndedAt(null);
        this.changeVersionReviewState(versionId, ReviewState.UNDER_REVIEW, false);
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg.getMessage(), new JSONB(msg.getArgs()), reviewAction));
        this.projectVersionReviewsDAO.update(projectVersionReviewTable);
    }

    @Transactional
    public void approveReview(final long versionId, final ReviewMessage msg, final ReviewState reviewState, final ReviewAction reviewAction) {
        final ProjectVersionReviewTable latestUnfinishedReview = this.getLatestUnfinishedReviewAndValidate(versionId);
        latestUnfinishedReview.setEndedAt(OffsetDateTime.now());
        final boolean isLastUnfinished = this.projectVersionReviewsDAO.getUnfinishedReviews(versionId).size() == 1;
        if (isLastUnfinished) { // only unfinished is the one about to be finished
            this.changeVersionReviewState(versionId, reviewState, true);
        }
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.getMessage(), new JSONB(msg.getArgs()), reviewAction));
        this.projectVersionReviewsDAO.update(latestUnfinishedReview);
        final ProjectVersionTable projectVersionTable = this.projectVersionsDAO.getProjectVersionTable(versionId);
        if (isLastUnfinished) {
            this.notificationService.notifyUsersVersionReviewed(projectVersionTable, reviewAction == ReviewAction.PARTIALLY_APPROVE);
        }
    }

    @Transactional
    public void undoApproval(final long versionId, final ReviewMessage msg) {
        final ProjectVersionReviewMessageTable reviewMessageTable = this.projectVersionReviewsDAO.getLatestMessage(versionId, this.getHangarPrincipal().getUserId());
        if (!reviewMessageTable.getAction().isApproval()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.badUndo");
        }
        this.reopenReview(versionId, msg, ReviewAction.UNDO_APPROVAL);
    }

    public void changeVersionReviewState(final long versionId, final ReviewState reviewState, final boolean approve) {
        ProjectVersionTable projectVersionTable = this.projectVersionsDAO.getProjectVersionTable(versionId);
        if (projectVersionTable.getReviewState() != reviewState) {
            final ReviewState oldState = projectVersionTable.getReviewState();
            projectVersionTable.setReviewState(reviewState);
            this.actionLogger.version(LogAction.VERSION_REVIEW_STATE_CHANGED.create(VersionContext.of(projectVersionTable.getProjectId(), versionId), reviewState.getFrontendName(), oldState.getFrontendName()));
            if (approve) {
                projectVersionTable.setReviewerId(this.getHangarPrincipal().getUserId());
                projectVersionTable.setApprovedAt(OffsetDateTime.now());
                projectVersionTable = this.projectVersionsDAO.update(projectVersionTable);
                this.projectVersionVisibilityService.changeVisibility(projectVersionTable, Visibility.PUBLIC, "visibility.changes.version.reviewed");
            } else {
                this.projectVersionsDAO.update(projectVersionTable);
            }
        }
    }

    private @NotNull ProjectVersionReviewTable getLatestUnfinishedReviewAndValidate(final long versionId) {
        final ProjectVersionReviewTable latestUnfinishedReview = this.projectVersionReviewsDAO.getLatestUnfinishedReview(versionId, this.getHangarPrincipal().getUserId());
        if (latestUnfinishedReview == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.noReviewStarted");
        } else if (!Objects.equals(latestUnfinishedReview.getUserId(), this.getHangarUserId())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.notCorrectUser");
        }
        return latestUnfinishedReview;
    }

    public List<HangarReviewQueueEntry> getReviewQueue(final ReviewState reviewState) {
        return this.hangarReviewsDAO.getReviewQueue(reviewState);
    }

    public int getApprovalQueueSize() {
        return this.hangarReviewsDAO.getReviewQueueSize(ReviewState.UNREVIEWED);
    }
}
