package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.versions.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PermissionRequired(type = PermissionType.GLOBAL, perms = NamedPermission.REVIEWER)
    @GetMapping(value = "/{versionId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HangarReview>> getVersionReviews(@PathVariable long versionId) {
        return ResponseEntity.ok(reviewService.getHangarReviews(versionId));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.GLOBAL, perms = NamedPermission.REVIEWER)
    @PostMapping("/{versionId}/reviews/start")
    public void startVersionReview(@PathVariable long versionId, @Valid @RequestBody StringContent msg) {
        reviewService.startReview(versionId, msg.getContent());
    }
}
