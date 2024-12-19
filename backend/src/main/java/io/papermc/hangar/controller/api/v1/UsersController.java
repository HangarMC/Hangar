package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IUsersController;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.api.UsersApiService;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Anyone
@Controller
@RateLimit(path = "apiusers", greedy = true)
public class UsersController extends HangarComponent implements IUsersController {

    private final UsersApiService usersApiService;

    @Autowired
    public UsersController(final UsersApiService usersApiService) {
        this.usersApiService = usersApiService;
    }

    @Override
    public ResponseEntity<User> getUser(final UserTable user) {
        return ResponseEntity.ok(this.usersApiService.getUser(user.getId(), User.class));
    }

    @Override
    @ApplicableSorters({SorterRegistry.USER_NAME, SorterRegistry.USER_JOIN_DATE, SorterRegistry.USER_PROJECT_COUNT, SorterRegistry.USER_LOCKED, SorterRegistry.USER_ORG, SorterRegistry.USER_ROLES})
    public ResponseEntity<PaginatedResult<User>> getUsers(final String query, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getUsers(query, pagination, User.class));
    }

    @Override
    @ApplicableSorters({SorterRegistry.VIEWS, SorterRegistry.DOWNLOADS, SorterRegistry.NEWEST, SorterRegistry.STARS, SorterRegistry.UPDATED, SorterRegistry.RECENT_DOWNLOADS, SorterRegistry.RECENT_VIEWS, SorterRegistry.SLUG})
    public ResponseEntity<PaginatedResult<ProjectCompact>> getUserStarred(final UserTable user, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getUserStarred(user, pagination));
    }

    @Override
    @ApplicableSorters({SorterRegistry.VIEWS, SorterRegistry.DOWNLOADS, SorterRegistry.NEWEST, SorterRegistry.STARS, SorterRegistry.UPDATED, SorterRegistry.RECENT_DOWNLOADS, SorterRegistry.RECENT_VIEWS, SorterRegistry.SLUG})
    public ResponseEntity<PaginatedResult<ProjectCompact>> getUserWatching(final UserTable user, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getUserWatching(user, pagination));
    }

    @Override
    public ResponseEntity<List<ProjectCompact>> getUserPinnedProjects(final UserTable user) {
        return ResponseEntity.ok(this.usersApiService.getUserPinned(user));
    }

    @Override
    @ApplicableSorters({SorterRegistry.USER_NAME, SorterRegistry.USER_JOIN_DATE, SorterRegistry.USER_PROJECT_COUNT})
    public ResponseEntity<PaginatedResult<User>> getAuthors(final String query, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getAuthors(query, pagination));
    }

    @Override
    @ApplicableSorters({SorterRegistry.USER_NAME, SorterRegistry.USER_JOIN_DATE, SorterRegistry.USER_ROLES})
    public ResponseEntity<PaginatedResult<User>> getStaff(final String query, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getStaff(query, pagination));
    }
}
