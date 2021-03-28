package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.api.v1.interfaces.IUsersController;
import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableSorters;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.service.api.UsersApiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class UsersController extends HangarController implements IUsersController {

    private final UsersApiService usersApiService;

    @Autowired
    public UsersController(UsersApiService usersApiService) {
        this.usersApiService = usersApiService;
    }

    @Override
    public ResponseEntity<User> getUser(String userName) {
        return ResponseEntity.ok(usersApiService.getUser(userName, User.class));
    }

    @Override
    public ResponseEntity<PaginatedResult<User>> getUsers(String query, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(usersApiService.getUsers(query, pagination, User.class));
    }

    @Override
    public ResponseEntity<PaginatedResult<ProjectCompact>> getUserStarred(String userName, ProjectSortingStrategy sort, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(usersApiService.getUserStarred(userName, sort, pagination));
    }

    @Override
    public ResponseEntity<PaginatedResult<ProjectCompact>> getUserWatching(String userName, ProjectSortingStrategy sort, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(usersApiService.getUserWatching(userName, sort, pagination));
    }

    @Override
    @ApplicableSorters({ SorterRegistry.USERNAME_VALUE, SorterRegistry.JOINED_VALUE, SorterRegistry.PROJECT_COUNT_VALUE })
    public ResponseEntity<PaginatedResult<User>> getAuthors(@NotNull RequestPagination pagination) {
        return ResponseEntity.ok(usersApiService.getAuthors(pagination));
    }

    @Override
    @ApplicableSorters({ SorterRegistry.USERNAME_VALUE, SorterRegistry.JOINED_VALUE })
    public ResponseEntity<PaginatedResult<User>> getStaff(@NotNull RequestPagination pagination) {
        return ResponseEntity.ok(usersApiService.getStaff( pagination));
    }
}
