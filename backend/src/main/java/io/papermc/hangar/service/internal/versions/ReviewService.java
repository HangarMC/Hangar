package io.papermc.hangar.service.internal.versions;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionReviewsDAO;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.db.dao.internal.versions.HangarReviewsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PlatformVersionDownload;
import io.papermc.hangar.model.api.project.version.VersionToScan;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.ReviewAction;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewMessageTable;
import io.papermc.hangar.model.db.versions.reviews.ProjectVersionReviewTable;
import io.papermc.hangar.model.internal.api.requests.versions.ReviewMessage;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.model.internal.versions.HangarReviewQueueEntry;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.visibility.ProjectVersionVisibilityService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService extends HangarComponent {

    private static final JSONB EMPTY_JSON = new JSONB("{}");
    private final ProjectVersionReviewsDAO projectVersionReviewsDAO;
    private final HangarReviewsDAO hangarReviewsDAO;
    private final ProjectVersionsDAO projectVersionsDAO;
    private final ProjectVersionVisibilityService projectVersionVisibilityService;
    private final NotificationService notificationService;
    private final ProjectsDAO projectsDAO;
    private final DownloadService downloadService;
    private final UserService userService;

    @Autowired
    public ReviewService(final ProjectVersionReviewsDAO projectVersionReviewsDAO, final HangarReviewsDAO hangarReviewsDAO, final ProjectVersionsDAO projectVersionsDAO, final ProjectVersionVisibilityService projectVersionVisibilityService, final NotificationService notificationService, final ProjectsDAO projectsDAO, final DownloadService downloadService, final UserService userService) {
        this.projectVersionReviewsDAO = projectVersionReviewsDAO;
        this.hangarReviewsDAO = hangarReviewsDAO;
        this.projectVersionsDAO = projectVersionsDAO;
        this.projectVersionVisibilityService = projectVersionVisibilityService;
        this.notificationService = notificationService;
        this.projectsDAO = projectsDAO;
        this.downloadService = downloadService;
        this.userService = userService;
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
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg.message(), new JSONB(msg.args()), ReviewAction.START));
        this.changeVersionReviewState(versionId, ReviewState.UNDER_REVIEW, false);
    }

    @Transactional
    public void autoReviewFiles(final VersionToScan version, final boolean partial, final long scannerUserId) {
        final ReviewState oldReviewState = version.reviewState();
        if (oldReviewState == ReviewState.REVIEWED) {
            return;
        }

        final ReviewState reviewState = partial ? ReviewState.PARTIALLY_REVIEWED : ReviewState.REVIEWED;
        if (oldReviewState == reviewState) {
            return;
        }

        final ProjectVersionReviewTable projectVersionReviewTable = this.insertOrUpdateReview(version.versionId(), scannerUserId);
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(
            projectVersionReviewTable.getId(),
            "[AUTO] Automated review via jar scan",
            EMPTY_JSON,
            partial ? ReviewAction.PARTIALLY_APPROVE : ReviewAction.APPROVE
        ));

        final ProjectVersionTable projectVersionTable = this.projectVersionsDAO.getProjectVersionTable(version.versionId());
        projectVersionTable.setReviewState(reviewState);
        this.projectVersionsDAO.update(projectVersionTable);
    }

    @Transactional
    public void autoReviewLinks(final ProjectVersionTable projectVersionTable, final long scannerUserId) {
        final ProjectVersionReviewTable projectVersionReviewTable = this.insertOrUpdateReview(projectVersionTable.getVersionId(), scannerUserId);
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(
            projectVersionReviewTable.getId(),
            "[AUTO] Automated review via safe link check",
            EMPTY_JSON,
            ReviewAction.APPROVE
        ));

        projectVersionTable.setReviewState(ReviewState.REVIEWED);
        this.projectVersionsDAO.update(projectVersionTable);
    }

    private ProjectVersionReviewTable insertOrUpdateReview(final long versionId, final long scannerUserId) {
        ProjectVersionReviewTable projectVersionReviewTable = this.projectVersionReviewsDAO.getUsersReview(versionId, scannerUserId);
        if (projectVersionReviewTable != null) {
            projectVersionReviewTable.setEndedAt(OffsetDateTime.now());
            this.projectVersionReviewsDAO.update(projectVersionReviewTable);
            return projectVersionReviewTable;
        }

        projectVersionReviewTable = new ProjectVersionReviewTable(versionId, scannerUserId);
        projectVersionReviewTable.setEndedAt(OffsetDateTime.now());
        return this.projectVersionReviewsDAO.insert(projectVersionReviewTable);
    }

    @Transactional
    public void addReviewMessage(final long versionId, final ReviewMessage msg) {
        final ProjectVersionReviewTable latestUnfinishedReview = this.getLatestUnfinishedReviewAndValidate(versionId);
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.message(), new JSONB(msg.args()), ReviewAction.MESSAGE));
    }

    @Transactional
    public void stopReview(final long versionId, final ReviewMessage msg) {
        final ProjectVersionReviewTable latestUnfinishedReview = this.getLatestUnfinishedReviewAndValidate(versionId);
        latestUnfinishedReview.setEndedAt(OffsetDateTime.now());
        if (this.projectVersionReviewsDAO.getUnfinishedReviews(versionId).size() == 1) { // only unfinished is the one about to be finished
            this.changeVersionReviewState(versionId, ReviewState.UNREVIEWED, false);
        }
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.message(), new JSONB(msg.args()), ReviewAction.STOP));
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
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(projectVersionReviewTable.getId(), msg.message(), new JSONB(msg.args()), reviewAction));
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
        this.projectVersionReviewsDAO.insertMessage(new ProjectVersionReviewMessageTable(latestUnfinishedReview.getId(), msg.message(), new JSONB(msg.args()), reviewAction));
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

    private ProjectVersionReviewTable getLatestUnfinishedReviewAndValidate(final long versionId) {
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

    @Transactional
    public void approveVersionsWithSafeLinks() {
        // Slow and dumb, but doesn't matter
        final UserTable jarScannerUser = this.userService.getUserTable(JarScanningService.JAR_SCANNER_USER);
        final List<HangarReviewQueueEntry> reviewQueue = this.getReviewQueue(ReviewState.UNREVIEWED);
        for (final HangarReviewQueueEntry entry : reviewQueue) {
            final ProjectVersionTable projectVersionTable = this.projectVersionsDAO.getProjectVersionTable(entry.getVersionId());
            final ProjectTable projectTable = this.projectsDAO.getById(projectVersionTable.getProjectId());
            final Map<Platform, PlatformVersionDownload> downloads = this.downloadService.getDownloads(projectTable.getOwnerName(), projectTable.getSlug(),
                projectVersionTable.getVersionString(), projectVersionTable.getVersionId());
            if (downloads.values().stream().anyMatch(download -> download.externalUrl() == null || !this.config.security.checkSafe(download.externalUrl()))) {
                continue;
            }

            this.autoReviewLinks(projectVersionTable, jarScannerUser.getUserId());
        }
    }
}
