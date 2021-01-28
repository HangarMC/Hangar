package io.papermc.hangar.service;

import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.controller.extras.requestmodels.api.RequestPagination;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UsersDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.internal.HangarUser;
import io.papermc.hangar.model.internal.HangarUser.HeaderData;
import io.papermc.hangar.modelold.ApiAuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService extends HangarService {

    private final UsersDAO usersDAO;
    private final NotificationsDAO notificationsDAO;
    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public UsersService(HangarDao<UsersDAO> usersDAO, HangarDao<NotificationsDAO> notificationsDAO, ApiAuthInfo apiAuthInfo) {
        this.usersDAO = usersDAO.get();
        this.notificationsDAO = notificationsDAO.get();
        this.apiAuthInfo = apiAuthInfo;
    }

    public <T extends User> T getUser(String name, Class<T> type) {
        T user = usersDAO.getUser(name, type);
        if (user == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a user with " + name + " name");
        }
        return user instanceof HangarUser ? (T) supplyHeaderData((HangarUser) user) : user;
    }

    public <T extends User> PaginatedResult<T> getUsers(String query, RequestPagination pagination, Class<T> type) {
        List<T> users = usersDAO.getUsers(query, pagination.getLimit(), pagination.getOffset(), type);
        return new PaginatedResult<>(new Pagination(usersDAO.getUsersCount(query), pagination), users);
    }

    public HangarUser supplyHeaderData(HangarUser hangarUser) {
        Permission globalPermission = apiAuthInfo.getGlobalPerms();

        boolean hasUnreadNotifs = notificationsDAO.hasUnreadNotifications(apiAuthInfo.getUserId());
        boolean hasUnresolvedFlags = globalPermission.has(Permission.ModNotesAndFlags) && notificationsDAO.hasUnresolvedFlags();
        boolean hasProjectApprovals = globalPermission.has(Permission.ModNotesAndFlags.add(Permission.SeeHidden)) && notificationsDAO.hasProjectApprovals(apiAuthInfo.getUserId());
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
