package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UsersDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.db.dao.v1.UsersApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.api.project.ProjectSortingStrategy;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.model.internal.user.HangarUser.HeaderData;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.organizations.OrganizationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiFunction;

@Service
public class UsersApiService extends HangarComponent {

    private final UsersDAO usersDAO;
    private final UsersApiDAO usersApiDAO;
    private final NotificationsDAO notificationsDAO;
    private final PermissionService permissionService;
    private final OrganizationService organizationService;

    @Autowired
    public UsersApiService(HangarDao<UsersDAO> usersDAO, HangarDao<UsersApiDAO> usersApiDAO, HangarDao<NotificationsDAO> notificationsDAO, PermissionService permissionService, OrganizationService organizationService) {
        this.usersDAO = usersDAO.get();
        this.usersApiDAO = usersApiDAO.get();
        this.notificationsDAO = notificationsDAO.get();
        this.permissionService = permissionService;
        this.organizationService = organizationService;
    }

    public <T extends User> T getUser(String name, Class<T> type) {
        T user = getUserRequired(name, usersDAO::getUser, type);
        return user instanceof HangarUser ? (T) supplyHeaderData((HangarUser) user) : user;
    }

    public <T extends User> PaginatedResult<T> getUsers(String query, RequestPagination pagination, Class<T> type) {
        List<T> users = usersDAO.getUsers(query, pagination.getLimit(), pagination.getOffset(), type);
        return new PaginatedResult<>(new Pagination(usersDAO.getUsersCount(query), pagination), users);
    }

    public PaginatedResult<ProjectCompact> getUserStarred(String userName, ProjectSortingStrategy sortingStrategy, RequestPagination pagination) {
        getUserRequired(userName, usersDAO::getUser, User.class);
        boolean canSeeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        List<ProjectCompact> projects = usersApiDAO.getUserStarred(userName, canSeeHidden, getHangarUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        long count = usersApiDAO.getUserStarredCount(userName, canSeeHidden, getHangarUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    public PaginatedResult<ProjectCompact> getUserWatching(String userName, ProjectSortingStrategy sortingStrategy, RequestPagination pagination) {
        getUserRequired(userName, usersDAO::getUser, User.class);
        boolean canSeeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        List<ProjectCompact> projects = usersApiDAO.getUserWatching(userName, canSeeHidden, getHangarUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        long count = usersApiDAO.getUserWatchingCount(userName, canSeeHidden, getHangarUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    @CacheEvict(value = CacheConfig.AUTHORS_CACHE,  allEntries = true)
    public void clearAuthorsCache() {}

    @Cacheable(CacheConfig.AUTHORS_CACHE)
    public PaginatedResult<User> getAuthors(RequestPagination pagination) {
        List<User> users = usersApiDAO.getAuthors(pagination);
        long count = usersApiDAO.getAuthorsCount();
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    @CacheEvict(value = CacheConfig.STAFF_CACHE,  allEntries = true)
    public void clearStaffCache() {}

    @Cacheable(CacheConfig.STAFF_CACHE)
    public PaginatedResult<User> getStaff(RequestPagination pagination) {
        List<User> users = usersApiDAO.getStaff(config.user.getStaffRoles(), pagination);
        long count = usersApiDAO.getStaffCount(config.user.getStaffRoles());
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
        long unresolvedFlags = globalPermission.has(Permission.ModNotesAndFlags) ? notificationsDAO.getUnresolvedFlagsCount() : 0;
        long projectApprovals = globalPermission.has(Permission.ModNotesAndFlags.add(Permission.SeeHidden)) ? notificationsDAO.getProjectApprovalsCount() : 0;
        long reviewQueueCount = globalPermission.has(Permission.Reviewer) ? notificationsDAO.getReviewQueueCount() : 0;
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
}
