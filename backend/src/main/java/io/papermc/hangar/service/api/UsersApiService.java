package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.UsersDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.db.dao.v1.UsersApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.model.internal.user.HangarUser.HeaderData;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.admin.FlagService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import io.papermc.hangar.service.internal.projects.PinnedProjectService;
import io.papermc.hangar.service.internal.projects.ProjectAdminService;
import io.papermc.hangar.service.internal.versions.ReviewService;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersApiService extends HangarComponent {

    private final UsersDAO usersDAO;
    private final UsersApiDAO usersApiDAO;
    private final NotificationsDAO notificationsDAO;
    private final PermissionService permissionService;
    private final OrganizationService organizationService;
    private final PinnedProjectService pinnedProjectService;
    private final ReviewService reviewService;
    private final ProjectAdminService projectAdminService;
    private final FlagService flagService;

    @Autowired
    public UsersApiService(UsersDAO usersDAO, UsersApiDAO usersApiDAO, NotificationsDAO notificationsDAO, PermissionService permissionService, OrganizationService organizationService, final PinnedProjectService pinnedProjectService, final ReviewService reviewService, @Lazy final ProjectAdminService projectAdminService, final FlagService flagService) {
        this.usersDAO = usersDAO;
        this.usersApiDAO = usersApiDAO;
        this.notificationsDAO = notificationsDAO;
        this.permissionService = permissionService;
        this.organizationService = organizationService;
        this.pinnedProjectService = pinnedProjectService;
        this.reviewService = reviewService;
        this.projectAdminService = projectAdminService;
        this.flagService = flagService;
    }

    public <T extends User> T getUser(String name, Class<T> type) {
        T user = getUserRequired(name, usersDAO::getUser, type);
        this.supplyNameHistory(user);
        return user instanceof HangarUser ? (T) supplyHeaderData((HangarUser) user) : user;
    }

    @Transactional
    public <T extends User> PaginatedResult<T> getUsers(String query, RequestPagination pagination, Class<T> type) {
        boolean hasQuery = !StringUtils.isBlank(query);
        List<T> users = usersDAO.getUsers(hasQuery, query, pagination, type);
        return new PaginatedResult<>(new Pagination(usersDAO.getUsersCount(hasQuery, query), pagination), users);
    }

    @Transactional
    public PaginatedResult<ProjectCompact> getUserStarred(String userName, ProjectSortingStrategy sortingStrategy, RequestPagination pagination) {
        getUserRequired(userName, usersDAO::getUser, User.class);
        boolean canSeeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        List<ProjectCompact> projects = usersApiDAO.getUserStarred(userName, canSeeHidden, getHangarUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        long count = usersApiDAO.getUserStarredCount(userName, canSeeHidden, getHangarUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    @Transactional
    public PaginatedResult<ProjectCompact> getUserWatching(String userName, ProjectSortingStrategy sortingStrategy, RequestPagination pagination) {
        getUserRequired(userName, usersDAO::getUser, User.class);
        boolean canSeeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        List<ProjectCompact> projects = usersApiDAO.getUserWatching(userName, canSeeHidden, getHangarUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        long count = usersApiDAO.getUserWatchingCount(userName, canSeeHidden, getHangarUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    @CacheEvict(value = CacheConfig.AUTHORS, allEntries = true)
    public void clearAuthorsCache() {
        // Clears a cache
    }

    @Cacheable(CacheConfig.AUTHORS)
    @Transactional
    public PaginatedResult<User> getAuthors(RequestPagination pagination) {
        List<User> users = usersApiDAO.getAuthors(pagination);
        long count = usersApiDAO.getAuthorsCount();
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    @CacheEvict(value = CacheConfig.STAFF, allEntries = true)
    public void clearStaffCache() {
        // Clears a cache
    }

    @Cacheable(CacheConfig.STAFF)
    @Transactional
    public PaginatedResult<User> getStaff(RequestPagination pagination) {
        List<User> users = usersApiDAO.getStaff(config.user.staffRoles(), pagination);
        long count = usersApiDAO.getStaffCount(config.user.staffRoles());
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    @NotNull
    private <T, U extends User> U getUserRequired(@Nullable T identifier, @NotNull BiFunction<T, Class<U>, U> function, @NotNull Class<U> type) {
        if (identifier == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        U user = function.apply(identifier, type);
        if (user == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a user with identifier: " + identifier);
        }
        return user;
    }

    public HangarUser supplyHeaderData(HangarUser hangarUser) {
        Permission globalPermission = permissionService.getGlobalPermissions(hangarUser.getId());
        long unreadNotifs = notificationsDAO.getUnreadNotificationCount(hangarUser.getId());
        long unansweredInvites = notificationsDAO.getUnansweredInvites(hangarUser.getId());
        long unresolvedFlags = globalPermission.has(Permission.ModNotesAndFlags) ? flagService.getFlagsQueueSize(false) : 0;
        long projectApprovals = globalPermission.has(Permission.ModNotesAndFlags.add(Permission.SeeHidden)) ? projectAdminService.getApprovalQueueSize() : 0;
        long reviewQueueCount = globalPermission.has(Permission.Reviewer) ? reviewService.getApprovalQueueSize() : 0;
        long organizationCount = organizationService.getUserOrganizationCount(hangarUser.getId());
        hangarUser.setHeaderData(new HeaderData(
            globalPermission,
            unreadNotifs,
            unansweredInvites,
            unresolvedFlags,
            projectApprovals,
            reviewQueueCount,
            organizationCount));
        return hangarUser;
    }

    public void supplyNameHistory(final User user) {
        final Optional<HangarPrincipal> hangarPrincipal = this.getOptionalHangarPrincipal();
        final List<UserNameChange> userNameHistory;
        if (hangarPrincipal.isPresent() && hangarPrincipal.get().isAllowedGlobal(Permission.SeeHidden)) {
            userNameHistory = this.usersApiDAO.getUserNameHistory(user.getName(),  OffsetDateTime.MIN);
        } else {
            userNameHistory = this.usersApiDAO.getUserNameHistory(user.getName(), OffsetDateTime.now().minus(this.config.user.nameChangeHistory(), ChronoUnit.DAYS));
        }
        user.setNameHistory(userNameHistory);
    }

    public List<ProjectCompact> getUserPinned(String userName) {
        return pinnedProjectService.getPinnedVersions(getUserRequired(userName, usersDAO::getUser, HangarUser.class).getId());
    }
}
