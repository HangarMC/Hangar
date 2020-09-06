package io.papermc.hangar.controller;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.db.model.ProjectVersionReviewsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.VersionData;
import io.papermc.hangar.model.viewhelpers.VersionReview;
import io.papermc.hangar.model.viewhelpers.VersionReviewMessage;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.ReviewService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Controller
public class ReviewsController extends HangarController {

    private final ProjectService projectService;
    private final VersionService versionService;
    private final ReviewService reviewService;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final UserService userService;

    private final HttpServletRequest request;

    @Autowired
    public ReviewsController(ProjectService projectService, VersionService versionService, ReviewService reviewService, NotificationService notificationService, UserActionLogService userActionLogService, UserService userService, HttpServletRequest request, Supplier<Optional<UsersTable>> currentUser) {
        this.projectService = projectService;
        this.versionService = versionService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
        this.userActionLogService = userActionLogService;
        this.userService = userService;
        this.request = request;
        this.currentUser = currentUser;
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/versions/{version}/reviews")
    public ModelAndView showReviews(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ModelAndView mav = new ModelAndView("users/admin/reviews");
        VersionData versionData = versionService.getVersionData(author, slug, version);
        mav.addObject("version", versionData);
        mav.addObject("project", versionData.getP());
        List<VersionReview> rv = reviewService.getRecentReviews(versionData.getV().getId());
        mav.addObject("reviews", rv);
        ProjectVersionReviewsTable unfinished = rv.stream().filter(review -> review.getEndedAt() == null).findFirst().orElse(null);
        mav.addObject("mostRecentUnfinishedReview", unfinished);
        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/reviews/addmessage", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> addMessage(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String content) {
        UsersTable curUser = currentUser.get().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        VersionReview recentReview = reviewService.getMostRecentUnfinishedReview(versionsTable.getId());
        if (recentReview == null) {
            return new ResponseEntity<>("Review", HttpStatus.OK);
        }
        if (recentReview.getUserId() == curUser.getId()) {
            recentReview.addMessage(new VersionReviewMessage(content), reviewService);
        } else {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Review", HttpStatus.OK);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/approve")
    public ModelAndView approveReview(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        VersionReview review = reviewService.getMostRecentUnfinishedReview(versionsTable.getId());
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        review.setEndedAt(OffsetDateTime.now());
        reviewService.update(review);
        Map<UsersTable, Permission> users = projectService.getUsersPermissions(versionsTable.getProjectId());
        // TODO bulk insert
        users.forEach((user, perm) -> {
            if (perm.has(Permission.EditVersion)) {
                notificationService.sendNotification(user.getId(), null, NotificationType.VERSION_REVIEWED, new String[]{"notification.project.reviewed", slug, version});
            }
        });
        return Routes.REVIEWS_SHOW_REVIEWS.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/edit/{review}") // Pretty sure this isn't implemented
    public ResponseEntity<String> editReview(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @PathVariable("review") long reviewId, @RequestParam String content) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        VersionReview review = reviewService.getById(reviewId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        review.addMessage(new VersionReviewMessage(content), reviewService);
        return new ResponseEntity<>("Review" + review, HttpStatus.OK); //TODO
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/init")
    public ModelAndView createReview(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        ProjectVersionReviewsTable review = new ProjectVersionReviewsTable(
                versionsTable.getId(),
                getCurrentUser().getId(),
                new JSONB("{}")
        );
        reviewService.insert(review);
        return Routes.REVIEWS_SHOW_REVIEWS.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/reopen")
    public ModelAndView reopenReview(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        VersionReview review = reviewService.getRecentReviews(versionsTable.getId()).stream().findFirst().orElse(null);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        versionsTable.setReviewState(ReviewState.UNREVIEWED);
        versionsTable.setApprovedAt(null);
        versionsTable.setReviewerId(null);
        versionService.update(versionsTable);
        review.setEndedAt(null);

        review.addMessage(new VersionReviewMessage("Reopened the review", System.currentTimeMillis(), "start"), reviewService);
        reviewService.update(review);
        return Routes.REVIEWS_SHOW_REVIEWS.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/reviewtoggle")
    public ModelAndView backlogToggle(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        if (versionsTable.getReviewState() != ReviewState.BACKLOG && versionsTable.getReviewState() != ReviewState.UNREVIEWED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state for toggle backlog");
        }
        ReviewState oldState = versionsTable.getReviewState();
        ReviewState newState = oldState == ReviewState.BACKLOG ? ReviewState.UNREVIEWED : ReviewState.BACKLOG;
        versionsTable.setReviewState(newState);

        userActionLogService.version(request, LoggedActionType.VERSION_REVIEW_STATE_CHANGED.with(VersionContext.of(versionsTable.getProjectId(), versionsTable.getId())), newState.name(), oldState.name());
        versionService.update(versionsTable);
        return Routes.REVIEWS_SHOW_REVIEWS.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/versions/{version}/reviews/stop", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView stopReview(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String content) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        VersionReview review = reviewService.getMostRecentUnfinishedReview(versionsTable.getId());
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        review.setEndedAt(OffsetDateTime.now());
        review.addMessage(new VersionReviewMessage(content, System.currentTimeMillis(), "stop"), reviewService);
        reviewService.update(review);
        return Routes.REVIEWS_SHOW_REVIEWS.getRedirect(author, slug, version);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/takeover")
    public ModelAndView takeoverReview(@PathVariable String author, @PathVariable String slug, @PathVariable String version, @RequestParam String content) {
        ProjectVersionsTable versionsTable = versionService.getVersion(author, slug, version);
        VersionReview oldReview = reviewService.getMostRecentUnfinishedReview(versionsTable.getId());
        if (oldReview == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        oldReview.addMessage(new VersionReviewMessage(content, System.currentTimeMillis(), "takeover"), reviewService);
        reviewService.update(oldReview);

        reviewService.insert(new ProjectVersionReviewsTable(
                versionsTable.getId(),
                getCurrentUser().getId(),
                new JSONB("{}")
        ));
        return Routes.REVIEWS_SHOW_REVIEWS.getRedirect(author, slug, version);
    }

}

