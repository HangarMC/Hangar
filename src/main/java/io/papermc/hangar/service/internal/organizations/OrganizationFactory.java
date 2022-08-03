package io.papermc.hangar.service.internal.organizations;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.users.invites.ProjectInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationFactory extends HangarComponent {

    private final UserDAO userDAO;
    private final OrganizationDAO organizationDAO;
    private final OrganizationService organizationService;
    private final OrganizationMemberService organizationMemberService;
    private final GlobalRoleService globalRoleService;
    private final ProjectInviteService inviteService;
    private final ProjectsDAO projectsDAO;
    private final ProjectFactory projectFactory;

    @Autowired
    public OrganizationFactory(UserDAO userDAO, OrganizationDAO organizationDAO, OrganizationService organizationService, OrganizationMemberService organizationMemberService, GlobalRoleService globalRoleService, final ProjectInviteService inviteService, final ProjectsDAO projectsDAO, final ProjectFactory projectFactory) {
        this.userDAO = userDAO;
        this.organizationDAO = organizationDAO;
        this.organizationService = organizationService;
        this.organizationMemberService = organizationMemberService;
        this.globalRoleService = globalRoleService;
        this.inviteService = inviteService;
        this.projectsDAO = projectsDAO;
        this.projectFactory = projectFactory;
    }

    @Transactional
    public void createOrganization(String name) {
        if (!config.org.isEnabled()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.notEnabled");
        }
        if (organizationService.getOrganizationsOwnedBy(getHangarPrincipal().getId()).size() >= config.org.getCreateLimit()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.tooManyOrgs", config.org.getCreateLimit());
        }

        String dummyEmail = name.replaceAll("[^a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]", "") + '@' + config.org.getDummyEmailDomain();
        UserTable userTable = userDAO.create(UUID.randomUUID(), name, dummyEmail, "", "", "", List.of(), false, null);
        OrganizationTable organizationTable = organizationDAO.insert(new OrganizationTable(userTable.getId(), name, getHangarPrincipal().getId(), userTable.getId()));
        globalRoleService.addRole(GlobalRole.ORGANIZATION.create(null, userTable.getId(), false));
        organizationMemberService.addNewAcceptedByDefaultMember(OrganizationRole.ORGANIZATION_OWNER.create(organizationTable.getId(), getHangarPrincipal().getId(), true));
    }

    @Transactional
    public void deleteOrganization(final OrganizationTable organizationTable, final String comment) {
        // Move projects to organization owner and soft delete them
        final UserTable ownerTable = userDAO.getUserTable(organizationTable.getOwnerId());
        final List<ProjectTable> projects = projectsDAO.getUserProjects(organizationTable.getUserId(), true);
        for (final ProjectTable project : projects) {
            inviteService.setOwner(project, ownerTable, true);
            projectFactory.softDelete(project, comment);
        }

        // Hard delete organization
        organizationDAO.delete(organizationTable.getOrganizationId());
        userDAO.delete(organizationTable.getUserId());
    }
}
