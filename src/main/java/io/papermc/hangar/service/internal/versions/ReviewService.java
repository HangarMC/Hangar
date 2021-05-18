package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.HangarDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ReviewService extends HangarComponent {

    private final ProjectVersionReviewsDAO projectVersionReviewsDAO;
    private final HangarReviewsDAO hangarReviewsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final NotificationService notificationService;

    @Autowired
    public ReviewService(HangarDao<ProjectVersionReviewsDAO> projectVersionReviewsDAO, HangarDao<HangarReviewsDAO> hangarReviewsDAO, HangarDao<ProjectVersionsDAO> projectVersionsDAO, ProjectVersionVisibilityService projectVersionVisibilityService, NotificationService notificationService) {
        this.projectVersionReviewsDAO = projectVersionReviewsDAO.get();
        this.hangarReviewsDAO = hangarReviewsDAO.get();
        this.projectVersionsDAO = projectVersionsDAO.get();
        this.projectVersionVisibilityService = projectVersionVisibilityService;
        this.notificationService = notificationService;
    }

    public List<HangarReview> getHangarReviews(long versionId) {
        return hangarReviewsDAO.getReviews(versionId);
    }

    public void startReview(long versionId, ReviewMessage msg) {
        ProjectVersionReviewTable possibleUserVersion = projectVersionReviewsDAO.getUsersReview(versionId, getHangarPrincipal().getUserId());
        if (possibleUserVersion != null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.onlyOneReview");
        }
        ProjectVersionReviewTable projectVersionReviewTable = projectVersionReviewsDAO.insert(new ProjectVersionReviewTable(versionId, getHangarPrincipal().getUserId()));
        projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg.getMessage(), new JSONB(msg.getArgs()), ReviewAction.START));
        changeVersionReviewState(versionId, ReviewState.UNDER_REVIEW, false);
    }

    public void addReviewMessage(long versionId, ReviewMessage msg) {
        ProjectVersionReviewTable latestUnfinishedReview = getLatestUnfinishedReviewAndValidate(versionId);
        projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.getMessage(), new JSONB(msg.getArgs()), ReviewAction.MESSAGE));
    }

    public void stopReview(long versionId, ReviewMessage msg) {
        ProjectVersionReviewTable latestUnfinishedReview = getLatestUnfinishedReviewAndValidate(versionId);
        latestUnfinishedReview.setEndedAt(OffsetDateTime.now());
        if (projectVersionReviewsDAO.getUnfinishedReviews(versionId).size() == 1) { // only unfinished is the one about to be finished
            changeVersionReviewState(versionId, ReviewState.UNREVIEWED, false);
        }
        projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.getMessage(), new JSONB(msg.getArgs()), ReviewAction.STOP));
        projectVersionReviewsDAO.update(latestUnfinishedReview);
    }

    public void reopenReview(long versionId, ReviewMessage msg, ReviewAction reviewAction) {
        ProjectVersionReviewTable projectVersionReviewTable = projectVersionReviewsDAO.getUsersReview(versionId, getHangarPrincipal().getUserId());
        if (projectVersionReviewTable == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.noReviewStarted");
        }
        if (projectVersionReviewTable.getEndedAt() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.cannotReopen");
        }
        projectVersionReviewTable.setEndedAt(null);
        changeVersionReviewState(versionId, ReviewState.UNDER_REVIEW, false);
        projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg.getMessage(), new JSONB(msg.getArgs()), reviewAction));
        projectVersionReviewsDAO.update(projectVersionReviewTable);
    }

    public void approveReview(long versionId, ReviewMessage msg, ReviewState reviewState, ReviewAction reviewAction) {
        ProjectVersionReviewTable latestUnfinishedReview = getLatestUnfinishedReviewAndValidate(versionId);
        latestUnfinishedReview.setEndedAt(OffsetDateTime.now());
        boolean isLastUnfinished = projectVersionReviewsDAO.getUnfinishedReviews(versionId).size() == 1;
        if (isLastUnfinished) { // only unfinished is the one about to be finished
            changeVersionReviewState(versionId, reviewState, true);
        }
        projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.getMessage(), new JSONB(msg.getArgs()), reviewAction));
        projectVersionReviewsDAO.update(latestUnfinishedReview);
        ProjectVersionTable projectVersionTable = projectVersionsDAO.getProjectVersionTable(versionId);
        if (isLastUnfinished) {
            notificationService.notifyUsersVersionReviewed(projectVersionTable, reviewAction == ReviewAction.PARTIALLY_APPROVE);
        }
    }

    public void undoApproval(long versionId, ReviewMessage msg) {
        ProjectVersionReviewMessageTable reviewMessageTable = projectVersionReviewsDAO.getLatestMessage(versionId, getHangarPrincipal().getUserId());
        if (!reviewMessageTable.getAction().isApproval()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.badUndo");
        }
        reopenReview(versionId, msg, ReviewAction.UNDO_APPROVAL);
    }

    public void changeVersionReviewState(long versionId, ReviewState reviewState, boolean approve) {
        ProjectVersionTable projectVersionTable = projectVersionsDAO.getProjectVersionTable(versionId);
        if (projectVersionTable.getReviewState() != reviewState) {
            ReviewState oldState = projectVersionTable.getReviewState();
            projectVersionTable.setReviewState(reviewState);
            actionLogger.version(LogAction.VERSION_REVIEW_STATE_CHANGED.create(VersionContext.of(projectVersionTable.getProjectId(), versionId), reviewState.getFrontendName(), oldState.getFrontendName()));
            if (approve) {
                projectVersionTable.setReviewerId(getHangarPrincipal().getUserId());
                projectVersionTable.setApprovedAt(OffsetDateTime.now());
                projectVersionTable = projectVersionsDAO.update(projectVersionTable);
                projectVersionVisibilityService.changeVisibility(projectVersionTable, Visibility.PUBLIC, "visibility.changes.version.reviewed");
            }
            else {
                projectVersionsDAO.update(projectVersionTable);
            }
        }
    }

    @NotNull
    private ProjectVersionReviewTable getLatestUnfinishedReviewAndValidate(long versionId) {
        ProjectVersionReviewTable latestUnfinishedReview = projectVersionReviewsDAO.getLatestUnfinishedReview(versionId, getHangarPrincipal().getUserId());
        if (latestUnfinishedReview == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.noReviewStarted");
        } else if (!Objects.equals(latestUnfinishedReview.getUserId(), getHangarUserId())) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "reviews.error.notCorrectUser");
        }
        return latestUnfinishedReview;
    }

    public List<HangarReviewQueueEntry> getReviewQueue(ReviewState reviewState) {
        return hangarReviewsDAO.getReviewQueue(reviewState);
    }
}
