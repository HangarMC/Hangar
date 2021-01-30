package io.papermc.hangar.service.api;

import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.controller.extras.requestmodels.api.RequestPagination;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UsersDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.db.dao.v1.UsersApiDAO;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.project.ProjectCompact;
import io.papermc.hangar.model.internal.HangarUser;
import io.papermc.hangar.model.internal.HangarUser.HeaderData;
import io.papermc.hangar.modelold.UserOrdering;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersApiService extends HangarService {

    private final UsersDAO usersDAO;
    private final UsersApiDAO usersApiDAO;
    private final NotificationsDAO notificationsDAO;

    @Autowired
    public UsersApiService(HangarDao<UsersDAO> usersDAO, HangarDao<UsersApiDAO> usersApiDAO, HangarDao<NotificationsDAO> notificationsDAO) {
        this.usersDAO = usersDAO.get();
        this.usersApiDAO = usersApiDAO.get();
        this.notificationsDAO = notificationsDAO.get();
    }

    public <T extends User> T getUser(String name, Class<T> type) {
        T user = getUserRequired(name, type);
        return user instanceof HangarUser ? (T) supplyHeaderData((HangarUser) user) : user;
    }

    public <T extends User> PaginatedResult<T> getUsers(String query, RequestPagination pagination, Class<T> type) {
        List<T> users = usersDAO.getUsers(query, pagination.getLimit(), pagination.getOffset(), type);
        return new PaginatedResult<>(new Pagination(usersDAO.getUsersCount(query), pagination), users);
    }

    public PaginatedResult<ProjectCompact> getUserStarred(String userName, ProjectSortingStrategy sortingStrategy, RequestPagination pagination) {
        getUserRequired(userName, User.class);
        boolean canSeeHidden = hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden);
        List<ProjectCompact> projects = usersApiDAO.getUserStarred(userName, canSeeHidden, hangarApiRequest.getUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        long count = usersApiDAO.getUserStarredCount(userName, canSeeHidden, hangarApiRequest.getUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    public PaginatedResult<ProjectCompact> getUserWatching(String userName, ProjectSortingStrategy sortingStrategy, RequestPagination pagination) {
        getUserRequired(userName, User.class);
        boolean canSeeHidden = hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden);
        List<ProjectCompact> projects = usersApiDAO.getUserWatching(userName, canSeeHidden, hangarApiRequest.getUserId(), sortingStrategy.getSql(), pagination.getLimit(), pagination.getOffset());
        long count = usersApiDAO.getUserWatchingCount(userName, canSeeHidden, hangarApiRequest.getUserId());
        return new PaginatedResult<>(new Pagination(count, pagination), projects);
    }

    @CacheEvict(value = CacheConfig.AUTHORS_CACHE,  allEntries = true)
    public void clearAuthorsCache() {}

    @Cacheable(CacheConfig.AUTHORS_CACHE)
    public PaginatedResult<User> getAuthors(String sort, RequestPagination pagination) {
        List<User> users = usersApiDAO.getAuthors(pagination.getOffset(), pagination.getLimit(), userOrder(sort));
        long count = usersApiDAO.getAuthorsCount();
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    @CacheEvict(value = CacheConfig.STAFF_CACHE,  allEntries = true)
    public void clearStaffCache() {}

    @Cacheable(CacheConfig.STAFF_CACHE)
    public PaginatedResult<User> getStaff(String sort, RequestPagination pagination) {
        List<User> users = usersApiDAO.getStaff(pagination.getOffset(), pagination.getLimit(), userOrder(sort));
        long count = usersApiDAO.getStaffCount();
        return new PaginatedResult<>(new Pagination(count, pagination), users);
    }

    private String userOrder(String sortStr) {
        boolean reverse = false;
        if (sortStr.startsWith("-")) {
            sortStr = sortStr.substring(1);
            reverse = true;
        }

        String sort = reverse ? " ASC" : " DESC";

        String sortUserName = "sq.name" + sort;
        String thenSortUserName = "," + sortUserName;

        switch (sortStr) {
            case UserOrdering.JoinDate:
                return "ORDER BY sq.join_date" + sort;
            case UserOrdering.UserName:
                return "ORDER BY " + sortUserName;
            case UserOrdering.Projects:
                return "ORDER BY sq.count" + sort + thenSortUserName;
            case UserOrdering.Role:
                return "ORDER BY sq.permission::BIGINT" + sort + " NULLS LAST" + ", sq.role" + sort + thenSortUserName;
            default:
                return " ";
        }
    }

    private <T extends User> T getUserRequired(String name, Class<T> type) {
        T user = usersDAO.getUser(name, type);
        if (user == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a user with " + name + " name");
        }
        return user;
    }

    private HangarUser supplyHeaderData(HangarUser hangarUser) {
        Permission globalPermission = hangarApiRequest.getGlobalPermissions();

        boolean hasUnreadNotifs = notificationsDAO.hasUnreadNotifications(hangarApiRequest.getUserId());
        boolean hasUnresolvedFlags = globalPermission.has(Permission.ModNotesAndFlags) && notificationsDAO.hasUnresolvedFlags();
        boolean hasProjectApprovals = globalPermission.has(Permission.ModNotesAndFlags.add(Permission.SeeHidden)) && notificationsDAO.hasProjectApprovals(hangarApiRequest.getUserId());
        boolean hasReviewQueue = globalPermission.has(Permission.Reviewer) && notificationsDAO.hasReviewQueue();
        hangarUser.setHeaderData(new HeaderData(
                globalPermission,
                hasUnreadNotifs || hasUnresolvedFlags || hasProjectApprovals || hasReviewQueue,
                hasUnreadNotifs,
                hasUnresolvedFlags,
                hasProjectApprovals,
                hasReviewQueue
        ));
        return hangarUser;
    }
}
