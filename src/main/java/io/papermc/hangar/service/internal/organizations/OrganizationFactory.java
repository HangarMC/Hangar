package io.papermc.hangar.service.internal.organizations;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.invites.OrganizationInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationFactory extends HangarComponent {

    private final UserDAO userDAO;
    private final OrganizationDAO organizationDAO;
    private final OrganizationService organizationService;
    private final OrganizationMemberService organizationMemberService;
    private final GlobalRoleService globalRoleService;

    @Autowired
    public OrganizationFactory(UserDAO userDAO, OrganizationDAO organizationDAO, OrganizationService organizationService, OrganizationMemberService organizationMemberService, GlobalRoleService globalRoleService) {
        this.userDAO = userDAO;
        this.organizationDAO = organizationDAO;
        this.organizationService = organizationService;
        this.organizationMemberService = organizationMemberService;
        this.globalRoleService = globalRoleService;
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
}
