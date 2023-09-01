package io.papermc.hangar.service.internal.organizations;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.customtypes.JSONB;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public OrganizationFactory(final UserDAO userDAO, final OrganizationDAO organizationDAO, final OrganizationService organizationService, final OrganizationMemberService organizationMemberService, final GlobalRoleService globalRoleService, final ProjectInviteService inviteService, final ProjectsDAO projectsDAO, final ProjectFactory projectFactory) {
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
    public OrganizationTable createOrganization(final String name) {
        if (!this.config.org.enabled()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.notEnabled");
        }
        if (this.organizationService.getOrganizationsOwnedBy(this.getHangarPrincipal().getId()).size() >= this.config.org.createLimit()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.tooManyOrgs", this.config.org.createLimit());
        }

        final String dummyEmail = name.replaceAll("[^a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]", "") + '@' + this.config.org.dummyEmailDomain();
        final UserTable userTable = this.userDAO.create(UUID.randomUUID(), name, dummyEmail, "", "", List.of(), false, null, true, new JSONB(Map.of()));
        final OrganizationTable organizationTable = this.organizationDAO.insert(new OrganizationTable(userTable.getId(), name, this.getHangarPrincipal().getId(), userTable.getId(), userTable.getUuid()));
        this.globalRoleService.addRole(GlobalRole.ORGANIZATION.create(null, userTable.getUuid(), userTable.getId(), false));
        this.organizationMemberService.addNewAcceptedByDefaultMember(OrganizationRole.ORGANIZATION_OWNER.create(organizationTable.getId(), userTable.getUuid(), this.getHangarPrincipal().getId(), true));
        return organizationTable;
    }

    @Transactional
    public void deleteOrganization(final OrganizationTable organizationTable, final String comment) {
        // Move projects to organization owner and soft delete them
        final UserTable ownerTable = this.userDAO.getUserTable(organizationTable.getOwnerId());
        final List<ProjectTable> projects = this.projectsDAO.getUserProjects(organizationTable.getUserId(), true);
        for (final ProjectTable project : projects) {
            this.inviteService.setOwner(project, ownerTable, true);
            this.projectFactory.softDelete(project, comment);
        }

        // Hard delete organization
        this.organizationDAO.delete(organizationTable.getOrganizationId());
        this.userDAO.delete(organizationTable.getUserId());
    }
}
