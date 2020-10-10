package io.papermc.hangar.controller.api;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.ApiAuthInfo;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.PaginatedUserResult;
import io.papermc.hangar.model.generated.PaginatedCompactProjectResult;
import io.papermc.hangar.model.generated.Pagination;
import io.papermc.hangar.model.generated.ProjectCompact;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.User;
import io.papermc.hangar.service.api.UserApiService;
import io.papermc.hangar.util.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@ApiController
@Controller
public class UsersApiController implements UsersApi {

    private static final Logger log = LoggerFactory.getLogger(UsersApiController.class);

    private final HangarConfig hangarConfig;
    private final ApiAuthInfo apiAuthInfo;
    private final UserApiService userApiService;

    @Autowired
    public UsersApiController(HangarConfig hangarConfig, ApiAuthInfo apiAuthInfo, UserApiService userApiService) {
        this.hangarConfig = hangarConfig;
        this.apiAuthInfo = apiAuthInfo;
        this.userApiService = userApiService;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<PaginatedCompactProjectResult> showStarred(String user, ProjectSortingStrategy sort, Long limit, Long offset) {
        long realLimit = ApiUtil.limitOrDefault(limit, hangarConfig.projects.getInitLoad());
        long realOffset = ApiUtil.offsetOrZero(offset);

        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);
        Long userId = apiAuthInfo.getUser() != null ? apiAuthInfo.getUser().getId() : null;

        List<ProjectCompact> projectCompactList = userApiService.getStarredProjects(user, seeHidden, userId, sort, realLimit, realOffset);
        long projectCount = userApiService.getStarredProjectsCount(user, seeHidden, userId);
        return ResponseEntity.ok(new PaginatedCompactProjectResult().result(projectCompactList).pagination(new Pagination().limit(realLimit).offset(realOffset).count(projectCount)));
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<User> showUser(String user) {
        User userObj = userApiService.getUser(user);
        if (userObj == null) {
            log.error("Couldn't find a user with " + user + " name!");
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a user with " + user + " name!");
        }
        return ResponseEntity.ok(userObj);
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<PaginatedUserResult> showUsers(String q, Long limit, long offset) {
        long realLimit = ApiUtil.limitOrDefault(limit, hangarConfig.projects.getInitLoad());
        long realOffset = ApiUtil.offsetOrZero(offset);

        List<User> users = userApiService.getUsers(q, realLimit, realOffset);
        long count = userApiService.getUsersCount(q);
        PaginatedUserResult result = new PaginatedUserResult(new Pagination().limit(realLimit).offset(realOffset).count(count), users);
        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<PaginatedCompactProjectResult> showWatching(String user, ProjectSortingStrategy sort, Long limit, Long offset) {
        long realLimit = ApiUtil.limitOrDefault(limit, hangarConfig.projects.getInitLoad());
        long realOffset = ApiUtil.offsetOrZero(offset);

        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);
        Long userId = apiAuthInfo.getUser() != null ? apiAuthInfo.getUser().getId() : null;

        List<ProjectCompact> projectCompactList = userApiService.getWatchedProjects(user, seeHidden, userId, sort, realLimit, realOffset);
        long projectCount = userApiService.getWatchedProjectsCount(user, seeHidden, userId);
        return ResponseEntity.ok(new PaginatedCompactProjectResult().result(projectCompactList).pagination(new Pagination().limit(realLimit).offset(realOffset).count(projectCount)));
    }
}
