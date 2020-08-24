package io.papermc.hangar.service;

import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.UserOrganizationRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.SsoSyncData;
import io.papermc.hangar.model.viewhelpers.FlagActivity;
import io.papermc.hangar.model.viewhelpers.ReviewActivity;
import io.papermc.hangar.service.sso.AuthUser;

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

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.OrganizationDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.UserOrdering;
import io.papermc.hangar.model.viewhelpers.Author;
import io.papermc.hangar.model.viewhelpers.HeaderData;
import io.papermc.hangar.model.viewhelpers.Staff;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.model.viewhelpers.UserRole;
import io.papermc.hangar.security.HangarAuthentication;

@Service
public class UserService {

    private final HangarDao<UserDao> userDao;
    private final HangarDao<OrganizationDao> orgDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<OrganizationDao> organizationDao;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final HangarConfig config;

    @Autowired
    public UserService(HangarDao<UserDao> userDao, HangarConfig config, HangarDao<OrganizationDao> orgDao, HangarDao<ProjectDao> projectDao, HangarDao<OrganizationDao> organizationDao, RoleService roleService, PermissionService permissionService) {
        this.userDao = userDao;
        this.config = config;
        this.orgDao = orgDao;
        this.projectDao = projectDao;
        this.organizationDao = organizationDao;
        this.roleService = roleService;
        this.permissionService = permissionService;
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
        boolean hasCurrentUser = getCurrentUser() != null;
        HeaderData headerData = new HeaderData(
                getCurrentUser(),
                hasCurrentUser ? permissionService.getGlobalPermissions(getCurrentUser().getId()) : PermissionService.DEFAULT_GLOBAL_PERMISSIONS,
                false,
                false,
                false,
                false,
                false
        );
        // TODO fill header data
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
        int projectCount = projectDao.get().getProjectCountByUserId(user.getId());
        Map<OrganizationsTable, UserOrganizationRolesTable> dbOrgs = orgDao.get().getUserOrganizationsAndRoles(user.getId());
        Map<OrganizationsTable, UserRole<UserOrganizationRolesTable>> organizations = new HashMap<>();
        dbOrgs.forEach((organization, userOrganizationRolesTable) -> {
            organizations.put(organization, new UserRole<>(userOrganizationRolesTable));
        });
        List<Role> globalRoles = roleService.getGlobalRolesForUser(user.getId(), null);
        boolean isOrga = globalRoles.contains(Role.ORGANIZATION);
        Permission userPerm = Permission.All; // TODO perms here
        Permission orgaPerm = Permission.None; // TODO perms here
        return new UserData(getHeaderData(), user, isOrga, projectCount, organizations, globalRoles, userPerm, orgaPerm);
    }

    public List<UsersTable> getUsers(List<String> userNames) {
        return userDao.get().getUsers(userNames);
    }

    public UsersTable getOrCreate(String username, AuthUser authUser) {
        UsersTable user = userDao.get().getByName(username);
        if (user == null) {
            user = new UsersTable(
                    authUser.getId(),
                    null,
                    authUser.getUsername(),
                    authUser.getEmail(),
                    null,
                    new int[0],
                    false,
                    authUser.getLang().toLanguageTag()
            );
            userDao.get().insert(user);
        }
        return user;
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
                    null
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

    public List<ReviewActivity> getReviewActivity(String username) {
        return userDao.get().getReviewActivity(username);
    }

    public List<FlagActivity> getFlagActivity(String username) {
        return userDao.get().getFlagActivity(username);
    }
}
