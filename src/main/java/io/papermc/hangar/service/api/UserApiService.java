package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.UsersApiDao;
import io.papermc.hangar.model.generated.ProjectCompact;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.User;
import io.papermc.hangar.util.ApiUtil;
import io.papermc.hangar.util.TemplateHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserApiService {

    private final HangarDao<UsersApiDao> usersApiDao;
    private final TemplateHelper templateHelper;

    public UserApiService(HangarDao<UsersApiDao> usersApiDao, TemplateHelper templateHelper) {
        this.usersApiDao = usersApiDao;
        this.templateHelper = templateHelper;
    }

    public User getUser(String name) {
        User user = usersApiDao.get().userQuery(name);
        if (user != null) {
            user.setAvatarUrl(templateHelper.avatarUrl(user.getName()));
        }
        return user;
    }

    public List<User> getUsers(String q, long limit, long offset) {
        return usersApiDao.get().usersQuery(q, limit, offset);
    }

    public long getUsersCount(String q) {
        return usersApiDao.get().usersQueryCount(q);
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
