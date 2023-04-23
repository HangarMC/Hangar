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
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.components.images.service.AvatarService;
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
    private final AvatarService avatarService;

    @Autowired
    public UsersApiService(final UsersDAO usersDAO, final UsersApiDAO usersApiDAO, final NotificationsDAO notificationsDAO, final PermissionService permissionService, final OrganizationService organizationService, final PinnedProjectService pinnedProjectService, final ReviewService reviewService, @Lazy final ProjectAdminService projectAdminService, final FlagService flagService, final AvatarService avatarService) {
        this.usersDAO = usersDAO;
        this.usersApiDAO = usersApiDAO;
        this.notificationsDAO = notificationsDAO;
        this.permissionService = permissionService;
        this.organizationService = organizationService;
        this.pinnedProjectService = pinnedProjectService;
        this.reviewService = reviewService;
        this.projectAdminService = projectAdminService;
        this.flagService = flagService;
        this.avatarService = avatarService;
    }

    public <T extends User> T getUser(final String name, final Class<T> type) {
        final T user = this.getUserRequired(name, this.usersDAO::getUser, type);
        this.supplyNameHistory(user);
        this.supplyAvatarUrl(user);
        return user instanceof HangarUser ? (T) this.supplyHeaderData((HangarUser) user) : user;
    }

    @Transactional(readOnly = true)
    public <T extends User> PaginatedResult<T> getUsers(final String query, final RequestPagination pagination, final Class<T> type) {
        final boolean hasQuery = !StringUtils.isBlank(query);
        final List<T> users = this.usersDAO.getUsers(hasQuery, query, pagination, type);
        users.forEach(u -> u.setAvatarUrl(this.avatarService.getUserAvatarUrl(u)));
        return new PaginatedResult<>(new Pagination(this.usersDAO.getUsersCount(hasQuery, query), pagination), users);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<ProjectCompact> getUserStarred(final String userName, final ProjectSortingStrategy sortingStrategy, final RequestPagination pagination) {
        this.getUserRequired(userName, this.usersDAO::getUser, User.class);
        final boolean canSeeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final List<ProjectCompact> projects = this.usersApiDAO.getUserStarred(userName, canSeeHidden, this.getHangarUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        projects.forEach(p -> p.setAvatarUrl(this.avatarService.getProjectAvatarUrl(p.getId(), p.getNamespace().getOwner())));
        final long count = this.usersApiDAO.getUserStarredCount(userName, canSeeHidden, this.getHangarUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<ProjectCompact> getUserWatching(final String userName, final ProjectSortingStrategy sortingStrategy, final RequestPagination pagination) {
        this.getUserRequired(userName, this.usersDAO::getUser, User.class);
        final boolean canSeeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final List<ProjectCompact> projects = this.usersApiDAO.getUserWatching(userName, canSeeHidden, this.getHangarUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        projects.forEach(p -> p.setAvatarUrl(this.avatarService.getProjectAvatarUrl(p.getId(), p.getNamespace().getOwner())));
        final long count = this.usersApiDAO.getUserWatchingCount(userName, canSeeHidden, this.getHangarUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    @CacheEvict(value = CacheConfig.AUTHORS, allEntries = true)
    public void clearAuthorsCache() {
        // Clears a cache
    }

    @Cacheable(CacheConfig.AUTHORS)
    @Transactional(readOnly = true)
    public PaginatedResult<User> getAuthors(final String query, final RequestPagination pagination) {
        final boolean hasQuery = !StringUtils.isBlank(query);
        final List<User> users = this.usersApiDAO.getAuthors(hasQuery, query, pagination);
        users.forEach(u -> u.setAvatarUrl(this.avatarService.getUserAvatarUrl(u)));
        final long count = this.usersApiDAO.getAuthorsCount();
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    @CacheEvict(value = CacheConfig.STAFF, allEntries = true)
    public void clearStaffCache() {
        // Clears a cache
    }

    @Cacheable(CacheConfig.STAFF)
    @Transactional(readOnly = true)
    public PaginatedResult<User> getStaff(final String query, final RequestPagination pagination) {
        final boolean hasQuery = !StringUtils.isBlank(query);
        final List<User> users = this.usersApiDAO.getStaff(hasQuery, query, this.config.user.staffRoles(), pagination);
        users.forEach(u -> u.setAvatarUrl(this.avatarService.getUserAvatarUrl(u)));
        final long count = this.usersApiDAO.getStaffCount(this.config.user.staffRoles());
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    private @NotNull <T, U extends User> U getUserRequired(final @Nullable T identifier, final @NotNull BiFunction<T, Class<U>, U> function, final @NotNull Class<U> type) {
        if (identifier == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        final U user = function.apply(identifier, type);
        if (user == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a user with identifier: " + identifier);
        }
        return user;
    }

    public HangarUser supplyHeaderData(final HangarUser hangarUser) {
        final Permission globalPermission = this.permissionService.getGlobalPermissions(hangarUser.getId());
        final long unreadNotifs = this.notificationsDAO.getUnreadNotificationCount(hangarUser.getId());
        final long unansweredInvites = this.notificationsDAO.getUnansweredInvites(hangarUser.getId());
        final long unresolvedFlags = globalPermission.has(Permission.ModNotesAndFlags) ? this.flagService.getFlagsQueueSize(false) : 0;
        final long projectApprovals = globalPermission.has(Permission.ModNotesAndFlags.add(Permission.SeeHidden)) ? this.projectAdminService.getApprovalQueueSize() : 0;
        final long reviewQueueCount = globalPermission.has(Permission.Reviewer) ? this.reviewService.getApprovalQueueSize() : 0;
        final long organizationCount = this.organizationService.getUserOrganizationCount(hangarUser.getId());
        hangarUser.setHeaderData(new HangarUser.HeaderData(
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
            userNameHistory = this.usersApiDAO.getUserNameHistory(user.getName(), OffsetDateTime.MIN);
        } else {
            userNameHistory = this.usersApiDAO.getUserNameHistory(user.getName(), OffsetDateTime.now().minus(this.config.user.nameChangeHistory(), ChronoUnit.DAYS));
        }
        user.setNameHistory(userNameHistory);
    }

    public void supplyAvatarUrl(final User user) {
        user.setAvatarUrl(this.avatarService.getUserAvatarUrl(user));
    }

    public List<ProjectCompact> getUserPinned(final String userName) {
        final List<ProjectCompact> pinnedVersions = this.pinnedProjectService.getPinnedVersions(this.getUserRequired(userName, this.usersDAO::getUser, HangarUser.class).getId());
        pinnedVersions.forEach(p -> p.setAvatarUrl(this.avatarService.getProjectAvatarUrl(p.getId(), p.getNamespace().getOwner())));
        return pinnedVersions;
    }
}
