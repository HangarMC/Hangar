package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IUsersController;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
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
    public ResponseEntity<User> getUser(final String userName) {
        return ResponseEntity.ok(this.usersApiService.getUser(userName, User.class));
    }

    @Override
    @ApplicableSorters({SorterRegistry.USER_NAME, SorterRegistry.USER_JOIN_DATE, SorterRegistry.USER_PROJECT_COUNT, SorterRegistry.USER_LOCKED, SorterRegistry.USER_ORG, SorterRegistry.USER_ROLES})
    public ResponseEntity<PaginatedResult<User>> getUsers(final String query, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getUsers(query, pagination, User.class));
    }

    @Override
    public ResponseEntity<PaginatedResult<ProjectCompact>> getUserStarred(final String userName, final ProjectSortingStrategy sort, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getUserStarred(userName, sort, pagination));
    }

    @Override
    public ResponseEntity<PaginatedResult<ProjectCompact>> getUserWatching(final String userName, final ProjectSortingStrategy sort, final @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(this.usersApiService.getUserWatching(userName, sort, pagination));
    }

    @Override
    public ResponseEntity<List<ProjectCompact>> getUserPinnedProjects(final String userName) {
        return ResponseEntity.ok(this.usersApiService.getUserPinned(userName));
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
