package io.papermc.hangar.service.internal.organizations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.MultiHangarApiException;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.perms.roles.OrganizationRoleService;
import io.papermc.hangar.service.internal.users.NotificationService;
import io.papermc.hangar.service.internal.users.invites.OrganizationInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException.UnprocessableEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationFactory extends HangarService {

    private final UserDAO userDAO;
    private final OrganizationDAO organizationDAO;
    private final OrganizationService organizationService;
    private final OrganizationMemberService organizationMemberService;
    private final OrganizationInviteService organizationInviteService;
    private final OrganizationRoleService organizationRoleService;
    private final GlobalRoleService globalRoleService;
    private final NotificationService notificationService;
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    @Autowired
    public OrganizationFactory(HangarDao<UserDAO> userDAO, HangarDao<OrganizationDAO> organizationDAO, OrganizationService organizationService, OrganizationMemberService organizationMemberService, OrganizationInviteService organizationInviteService, OrganizationRoleService organizationRoleService, GlobalRoleService globalRoleService, NotificationService notificationService, ObjectMapper mapper, RestTemplate restTemplate) {
        this.userDAO = userDAO.get();
        this.organizationDAO = organizationDAO.get();
        this.organizationService = organizationService;
        this.organizationMemberService = organizationMemberService;
        this.organizationInviteService = organizationInviteService;
        this.organizationRoleService = organizationRoleService;
        this.globalRoleService = globalRoleService;
        this.notificationService = notificationService;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public void createOrganization(String name, List<Member<OrganizationRole>> members) {
        if (!config.org.isEnabled()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.notEnabled");
        }
        if (organizationService.getOrganizationsOwnedBy(getHangarPrincipal().getId()).size() >= config.org.getCreateLimit()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.tooManyOrgs", config.org.getCreateLimit());
        }

        String dummyEmail = name.replaceAll("[^a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]", "") + '@' + config.org.getDummyEmailDomain();
        AuthUser authOrganizationUser;
        if (!config.fakeUser.isEnabled()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("api-key", config.sso.getApiKey());
            map.add("username", name);
            map.add("email", dummyEmail);
            map.add("verified", Boolean.TRUE.toString());
            map.add("dummy", Boolean.TRUE.toString());
            try {
                authOrganizationUser = mapper.treeToValue(restTemplate.postForObject(config.security.api.getUrl() + "/api/users", new HttpEntity<>(map, headers), ObjectNode.class), AuthUser.class);
            } catch (UnprocessableEntity e) {
                try {
                    ObjectNode objectNode = mapper.readValue(e.getResponseBodyAsByteArray(), ObjectNode.class);
                    List<HangarApiException> errors = new ArrayList<>();
                    for (JsonNode jsonNode : objectNode.get("error")) {
                        errors.add(new HangarApiException("organization.new.error.hangarAuthValidationError", jsonNode.asText()));
                    }
                    if (!errors.isEmpty()) {
                        throw new MultiHangarApiException(errors);
                    }
                } catch (IOException __) {
                    throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "organization.new.error.unknownError");
                }

                throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "organization.new.error.unknownError");
            } catch (JsonProcessingException e) {
                throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "organization.new.error.jsonError");
            }
        } else {
            authOrganizationUser = new AuthUser(name, dummyEmail);
            userDAO.insert(new UserTable(authOrganizationUser));
        }

        // Just a note, the /api/sync_sso creates the org user here, so it will already be created when the above response is returned
        UserTable userTable = userDAO.getUserTable(authOrganizationUser.getId());
        OrganizationTable organizationTable = organizationDAO.insert(new OrganizationTable(authOrganizationUser.getId(), name, getHangarPrincipal().getId(), userTable.getId()));
        globalRoleService.addRole(GlobalRole.ORGANIZATION.create(null, userTable.getId(), false));
        organizationMemberService.addNewAcceptedByDefaultMember(OrganizationRole.ORGANIZATION_OWNER.create(organizationTable.getId(), getHangarPrincipal().getId(), true));

        List<HangarApiException> errors = new ArrayList<>();
        organizationInviteService.sendInvites(errors, members, organizationTable.getId(), organizationTable.getName());

        if (!errors.isEmpty()) {
            throw new MultiHangarApiException(errors);
        }
    }
}
