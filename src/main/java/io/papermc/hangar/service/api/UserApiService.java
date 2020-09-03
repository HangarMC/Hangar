package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.UsersApiDao;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.generated.PaginatedCompactProjectResult;
import io.papermc.hangar.model.generated.ProjectCompact;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.User;
import io.papermc.hangar.util.ApiUtil;
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

    public List<ProjectCompact> getWatchedProjects(String user, boolean seeHidden, Long userId, ProjectSortingStrategy strategy, long limit, long offset) {
        return usersApiDao.get().watchersQuery(user, seeHidden, userId, ApiUtil.strategyOrDefault(strategy).getSql(), limit, offset);
    }

    public long getWatchedProjectsCount(String user, boolean seeHidden, Long userId) {
        return usersApiDao.get().watchersQuery(user, seeHidden, userId, ProjectSortingStrategy.Default.getSql(), null, 0).size();
    }

    public List<ProjectCompact> getStarredProjects(String user, boolean seeHidden, Long userId, ProjectSortingStrategy strategy, long limit, long offset) {
        return usersApiDao.get().starredQuery(user, seeHidden, userId, ApiUtil.strategyOrDefault(strategy).getSql(), limit, offset);
    }

    public long getStarredProjectsCount(String user, boolean seeHidden, Long userId) {
        return usersApiDao.get().starredQuery(user, seeHidden, userId, ProjectSortingStrategy.Default.getSql(), null, 0).size();
    }
}
