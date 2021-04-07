package io.papermc.hangar.controller.internal.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.internal.versions.HangarReviewQueueEntry;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.service.internal.projects.ProjectAdminService;
import io.papermc.hangar.service.internal.versions.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@PermissionRequired(NamedPermission.REVIEWER)
@RequestMapping(path = "/api/internal/admin/approval", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminApprovalController extends HangarComponent {

    private final ProjectAdminService projectAdminService;
    private final ReviewService reviewService;
    private final ObjectMapper mapper;

    @Autowired
    public AdminApprovalController(ProjectAdminService projectAdminService, ReviewService reviewService, ObjectMapper mapper) {
        this.projectAdminService = projectAdminService;
        this.reviewService = reviewService;
        this.mapper = mapper;
    }

    @GetMapping("/versions")
    public ResponseEntity<ObjectNode> getReviewQueue() {
        List<HangarReviewQueueEntry> underReviewEntries = reviewService.getReviewQueue(ReviewState.UNDER_REVIEW);
        List<HangarReviewQueueEntry> notStartedEntries = reviewService.getReviewQueue(ReviewState.UNREVIEWED);
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.set("underReview", mapper.valueToTree(underReviewEntries));
        objectNode.set("notStarted", mapper.valueToTree(notStartedEntries));
        return ResponseEntity.ok(objectNode);
    }

    @GetMapping("/projects")
    public ResponseEntity<ObjectNode> getProjectApprovals() {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.set("needsApproval", mapper.valueToTree(projectAdminService.getProjectsNeedingApproval()));
        objectNode.set("waitingProjects", mapper.valueToTree(projectAdminService.getProjectsWaitingForChanges()));
        return ResponseEntity.ok(objectNode);
    }
}
