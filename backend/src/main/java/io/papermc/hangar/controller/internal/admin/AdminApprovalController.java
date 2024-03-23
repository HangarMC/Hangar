package io.papermc.hangar.controller.internal.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.internal.projects.HangarProjectApproval;
import io.papermc.hangar.model.internal.versions.HangarReviewQueueEntry;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.projects.ProjectAdminService;
import io.papermc.hangar.service.internal.versions.ReviewService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Unlocked
@RestController
@RateLimit(path = "adminapproval")
@PermissionRequired(NamedPermission.REVIEWER)
@RequestMapping(path = "/api/internal/admin/approval", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminApprovalController extends HangarComponent {

    private final ProjectAdminService projectAdminService;
    private final ReviewService reviewService;

    @Autowired
    public AdminApprovalController(final ProjectAdminService projectAdminService, final ReviewService reviewService) {
        this.projectAdminService = projectAdminService;
        this.reviewService = reviewService;
    }

    @GetMapping("/versions")
    public ResponseEntity<ReviewQueue> getReviewQueue() {
        final List<HangarReviewQueueEntry> underReviewEntries = this.reviewService.getReviewQueue(ReviewState.UNDER_REVIEW);
        final List<HangarReviewQueueEntry> notStartedEntries = this.reviewService.getReviewQueue(ReviewState.UNREVIEWED);
        return ResponseEntity.ok(new ReviewQueue(underReviewEntries, notStartedEntries));
    }

    public record ReviewQueue(List<HangarReviewQueueEntry> underReview, List<HangarReviewQueueEntry> notStarted) { }

    @GetMapping("/projects")
    public ResponseEntity<ProjectApprovals> getProjectApprovals() {
        return ResponseEntity.ok(new ProjectApprovals(this.projectAdminService.getProjectsNeedingApproval(), this.projectAdminService.getProjectsWaitingForChanges()));
    }

    public record ProjectApprovals(List<HangarProjectApproval> needsApproval, List<HangarProjectApproval> waitingProjects) { }

    @GetMapping("/projectneedingapproval")
    public ResponseEntity<Integer> getProjectApprovalQueueSize() {
        return ResponseEntity.ok(this.projectAdminService.getApprovalQueueSize());
    }

    @GetMapping("/versionsneedingapproval")
    public ResponseEntity<Integer> getVersionApprovalQueueSize() {
        return ResponseEntity.ok(this.reviewService.getApprovalQueueSize());
    }
}
