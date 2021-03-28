package io.papermc.hangar.controllerold;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.db.modelold.ProjectVersionsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.VersionService;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Supplier;

@Controller
public class ReviewsController extends HangarController {

    private final VersionService versionService;
    private final UserActionLogService userActionLogService;

    private final HttpServletRequest request;
    private final Supplier<ProjectVersionsTable> projectVersionsTable;

    @Autowired
    public ReviewsController(VersionService versionService, UserActionLogService userActionLogService, HttpServletRequest request, Supplier<Optional<UsersTable>> currentUser, Supplier<ProjectVersionsTable> projectVersionsTable) {
        this.versionService = versionService;
        this.userActionLogService = userActionLogService;
        this.request = request;
        this.projectVersionsTable = projectVersionsTable;
        this.currentUser = currentUser;
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/versions/{version}/reviews/reviewtoggle")
    public ModelAndView backlogToggle(@PathVariable String author, @PathVariable String slug, @PathVariable String version) {
        ProjectVersionsTable versionsTable = projectVersionsTable.get();
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
}

