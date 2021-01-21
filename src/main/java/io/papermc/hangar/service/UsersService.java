package io.papermc.hangar.service;

import io.papermc.hangar.controller.requestmodels.api.RequestPagination;
import io.papermc.hangar.controllerold.exceptions.HangarApiException;
import io.papermc.hangar.db.dao.UsersDAO;
import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.internal.HangarUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    private final UsersDAO usersDAO;

    @Autowired
    public UsersService(HangarDao<UsersDAO> usersDAO) {
        this.usersDAO = usersDAO.get();
    }

//    public Supplier<HangarUser>

    public <T extends User> T getUser(String name, Class<T> type) {
        User user = usersDAO.getUser(name, type);
        if (user == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a user with " + name + " name");
        }
        return (T) user;
    }

    public <T extends User> PaginatedResult<T> getUsers(String query, RequestPagination pagination, Class<T> type) {
        List<T> users = usersDAO.getUsers(query, pagination.getLimit(), pagination.getOffset(), type);
        return new PaginatedResult<>(new Pagination(usersDAO.getUsersCount(query), pagination), users);
    }
}
