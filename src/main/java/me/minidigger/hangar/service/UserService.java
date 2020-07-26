package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import me.minidigger.hangar.config.CacheConfig;
import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.UserOrdering;
import me.minidigger.hangar.model.generated.Organization;
import me.minidigger.hangar.model.viewhelpers.Author;
import me.minidigger.hangar.model.viewhelpers.HeaderData;
import me.minidigger.hangar.model.viewhelpers.Staff;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.security.HangarAuthentication;

@Service
public class UserService {

    private final HangarDao<UserDao> userDao;
    private final HangarConfig config;

    @Autowired
    public UserService(HangarDao<UserDao> userDao, HangarConfig config) {
        this.userDao = userDao;
        this.config = config;
    }

    public UsersTable getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            HangarAuthentication auth = (HangarAuthentication) authentication;
//            User user = new User();
//            user.setName((String) authentication.getPrincipal());
//            user.setAvatarUrl("https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png");
//            user.setId("dummyid");
            return auth.getTable();
        }

        return null;
    }

    public HeaderData getHeaderData() {
        HeaderData headerData = new HeaderData();

        headerData.setCurrentUser(getCurrentUser());
        // TODO fill headerdata

        return headerData;
    }

    @CacheEvict(value = CacheConfig.AUTHORS_CACHE,  allEntries = true)
    public void clearAuthorsCache() {}

    @Cacheable(CacheConfig.AUTHORS_CACHE)
    public List<Author> getAuthors(int page, String sort) {
        boolean reverse = true;
        if (sort.startsWith("-")) {
            sort = sort.substring(1);
            reverse = false;
        }

        long pageSize = config.getAuthorPageSize();
        long offset = (page - 1) * pageSize;

        return userDao.get().getAuthors(offset, pageSize, userOrder(reverse, sort));
    }

    @CacheEvict(value = CacheConfig.STAFF_CACHE,  allEntries = true)
    public void clearStaffCache() {}

    @Cacheable(CacheConfig.STAFF_CACHE)
    public List<Staff> getStaff(int page, String sort) {
        boolean reverse = true;
        if (sort.startsWith("-")) {
            sort = sort.substring(1);
            reverse = false;
        }

        long pageSize = config.getAuthorPageSize();
        long offset = (page - 1) * pageSize;

        return userDao.get().getStaff(offset, pageSize, userOrder(reverse, sort));
    }

    private String userOrder(boolean reverse, String sortStr) {
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

    public UserData getUserData(UsersTable user) {
        // TODO getUserData
        boolean isOrga = false;
        int projectCount = 1;
        List<Organization> orgas = new ArrayList<>();
        List<Role> globalRoles = List.of(Role.HANGAR_ADMIN);
        Permission userPerm = Permission.All;
        Permission orgaPerm = Permission.None;
        return new UserData(getHeaderData(), user, isOrga, projectCount, orgas, globalRoles, userPerm, orgaPerm);
    }
}
