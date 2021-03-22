package io.papermc.hangar.service.internal.organizations;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarOrganizationsDAO;
import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.HangarOrganization;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService extends HangarService {

    private final HangarOrganizationsDAO hangarOrganizationsDAO;
    private final OrganizationDAO organizationDAO;
    private final UserDAO userDAO;
    private final PermissionService permissionService;

    @Autowired
    public OrganizationService(HangarDao<HangarOrganizationsDAO> hangarOrganizationsDAO, HangarDao<OrganizationDAO> organizationDAO, HangarDao<UserDAO> userDAO, PermissionService permissionService) {
        this.hangarOrganizationsDAO = hangarOrganizationsDAO.get();
        this.organizationDAO = organizationDAO.get();
        this.userDAO = userDAO.get();
        this.permissionService = permissionService;
    }

    public OrganizationTable getOrganizationTable(String name) {
        return organizationDAO.getByName(name);
    }

    public List<OrganizationTable> getOrganizationTablesWithPermission(long userId, Permission permission) {
        return organizationDAO.getOrganizationsWithPermission(userId, permission);
    }

    public List<OrganizationTable> getOrganizationsOwnedBy(long ownerId) {
        return organizationDAO.getOrganizationsOwnedBy(ownerId);
    }

    public HangarOrganization getHangarOrganization(String name) {
        OrganizationTable organizationTable = organizationDAO.getByName(name);
        if (organizationTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        UserTable owner = userDAO.getUserTable(organizationTable.getOwnerId());
        var members = hangarOrganizationsDAO.getOrganizationMembers(organizationTable.getId(), getHangarUserId(), permissionService.getOrganizationPermissions(getHangarUserId(), organizationTable.getId()).has(Permission.ManageOrganizationMembers));
        return new HangarOrganization(organizationTable.getId(), owner, members);
    }
}
