package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.OrganizationDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated(forRemoval = true)
public class OrgService extends HangarService {

    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<ProjectDao> projectDao;

    @Autowired
    public OrgService(HangarDao<OrganizationDao> organizationDao, HangarDao<UserDao> userDao, HangarDao<ProjectDao> projectDao) {
        this.organizationDao = organizationDao;
        this.userDao = userDao;
        this.projectDao = projectDao;
    }

    public OrganizationData getOrganizationData(UsersTable potentialOrg) {
        OrganizationsTable org = organizationDao.get().getByUserId(potentialOrg.getId());
        if (org == null) return null;
        return getOrganizationData(org, potentialOrg);
    }

    public OrganizationData getOrganizationData(OrganizationsTable org, @Nullable UsersTable orgUser) {
        UsersTable user = orgUser;
        if (orgUser == null) {
            user = userDao.get().getById(org.getUserId());
        }
        return new OrganizationData(org, user,organizationDao.get().getOrgMembers(org.getId()), projectDao.get().getProjectRoles(org.getUserId()));
    }
}
