package me.minidigger.hangar.service;

import me.minidigger.hangar.model.viewhelpers.FlagActivity;
import me.minidigger.hangar.model.viewhelpers.ReviewActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.minidigger.hangar.config.CacheConfig;
import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.OrganizationDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.SsoSyncData;
import me.minidigger.hangar.model.UserOrdering;
import me.minidigger.hangar.model.viewhelpers.Author;
import me.minidigger.hangar.model.viewhelpers.HeaderData;
import me.minidigger.hangar.model.viewhelpers.Staff;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.model.viewhelpers.UserRole;
import me.minidigger.hangar.security.HangarAuthentication;

@Service
public class UserService {

    private final HangarDao<UserDao> userDao;
    private final HangarDao<OrganizationDao> orgDao;
    private final HangarDao<ProjectDao> projectDao;
    private final RoleService roleService;
    private final HangarConfig config;

    @Autowired
    public UserService(HangarDao<UserDao> userDao, HangarConfig config, HangarDao<OrganizationDao> orgDao, HangarDao<ProjectDao> projectDao, RoleService roleService) {
        this.userDao = userDao;
        this.config = config;
        this.orgDao = orgDao;
        this.projectDao = projectDao;
        this.roleService = roleService;
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
        Permission global = Permission.HardDeleteProject.add(Permission.SeeHidden).add(Permission.ModNotesAndFlags);
        headerData.setGlobalPermission(headerData.getGlobalPermission().add(global)); // TODO remove

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

        long pageSize = config.user.getAuthorPageSize();
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

        long pageSize = config.user.getAuthorPageSize();
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

    public void setLocked(String userName, boolean locked) {
        UsersTable user = userDao.get().getByName(userName);
        user.setIsLocked(locked);
        userDao.get().update(user);
    }

    public UserData getUserData(long userId) {
        return getUserData(userDao.get().getById(userId));
    }

    public UserData getUserData(String userName) {
        return getUserData(userDao.get().getByName(userName));
    }

    public UserData getUserData(UsersTable user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // TODO getUserData
        boolean isOrga = false;
        int projectCount = projectDao.get().getProjectCountByUserId(user.getId());
        Map<OrganizationsTable, UserOrganizationRolesTable> dbOrgs = orgDao.get().getUserOrganizationsAndRoles(user.getId());
        Map<OrganizationsTable, UserRole<UserOrganizationRolesTable>> organizations = new HashMap<>();
        dbOrgs.forEach((organization, userOrganizationRolesTable) -> {
            organizations.put(organization, new UserRole<>(userOrganizationRolesTable));
        });
        List<Role> globalRoles = roleService.getGlobalRolesForUser(user.getId(), null);
        Permission userPerm = Permission.All;
        Permission orgaPerm = Permission.None;
        return new UserData(getHeaderData(), user, isOrga, projectCount, organizations, globalRoles, userPerm, orgaPerm);
    }

    public void ssoSyncUser(SsoSyncData syncData) {
        UsersTable user = userDao.get().getById(syncData.getExternalId());
        if (user == null) {
            user = new UsersTable(
                    syncData.getExternalId(),
                    syncData.getFullName(),
                    syncData.getUsername(),
                    syncData.getEmail(),
                    null,
                    new int[0],
                    false,
                    "en_US" // todo: how does language
            );
            user = userDao.get().insert(user);
        } else {
            user.setFullName(syncData.getFullName());
            user.setName(syncData.getUsername());
            user.setEmail(syncData.getEmail());
            userDao.get().update(user);
        }

        for (String roleName : syncData.getAddGroups()) {
            Role role = Role.fromValue(roleName);
            if (role != null) {
                roleService.addGlobalRole(user.getId(), role.getRoleId());
            }
        }

        for (String roleName : syncData.getRemoveGroups()) {
            Role role = Role.fromValue(roleName);
            if (role != null) {
                roleService.removeGlobalRole(user.getId(), role.getRoleId());
            }
        }
    }

    public List<ReviewActivity> getRewiewActivity(String username) {
        return userDao.get().getReviewActivity(username);
    }

    public List<FlagActivity> getFlagActivity(String username) {
        return userDao.get().getFlagActivity(username);
    }
}
