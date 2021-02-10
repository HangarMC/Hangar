package io.papermc.hangar.serviceold.apiold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.api.UsersApiDao;
import io.papermc.hangar.modelold.generated.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserApiService {

    private final HangarDao<UsersApiDao> usersApiDao;

    public UserApiService(HangarDao<UsersApiDao> usersApiDao) {
        this.usersApiDao = usersApiDao;
    }

    public User getUser(String name) {
        return usersApiDao.get().userQuery(name);
    }

    public List<User> getUsers(String q, long limit, long offset) {
        return usersApiDao.get().usersQuery(q, limit, offset);
    }

    public long getUsersCount(String q) {
        return usersApiDao.get().usersQueryCount(q);
    }
}
