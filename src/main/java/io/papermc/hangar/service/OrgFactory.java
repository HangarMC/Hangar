package io.papermc.hangar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.OrganizationContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.OrganizationDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.service.sso.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class OrgFactory {

    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<UserDao> userDao;
    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final RoleService roleService;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final HttpServletRequest request;

    @Autowired
    public OrgFactory(HangarDao<OrganizationDao> organizationDao, HangarDao<UserDao> userDao, HangarConfig hangarConfig, UserService userService, RoleService roleService, NotificationService notificationService, UserActionLogService userActionLogService, RestTemplate restTemplate, ObjectMapper mapper, HttpServletRequest request) {
        this.organizationDao = organizationDao;
        this.userDao = userDao;
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.roleService = roleService;
        this.notificationService = notificationService;
        this.userActionLogService = userActionLogService;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.request = request;
    }

    public OrganizationsTable createOrganization(String name, long ownerId, Map<Long, Role> members) {
        // TODO logging
        String dummyEmail = name.replaceAll("[^a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]", "") + '@' + hangarConfig.org.getDummyEmailDomain();
        AuthUser authOrgUser;
        if (!hangarConfig.fakeUser.isEnabled()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("api-key", hangarConfig.sso.getApiKey());
            map.add("username", name);
            map.add("email", dummyEmail);
            map.add("verified", Boolean.TRUE.toString());
            map.add("dummy", Boolean.TRUE.toString());
            try {
                authOrgUser = mapper.treeToValue(restTemplate.postForObject(hangarConfig.security.api.getUrl() + "/api/users", new HttpEntity<>(map, headers), ObjectNode.class), AuthUser.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new HangarException("error.org.cannotCreate");
            }
        } else {
            authOrgUser = new AuthUser(-100, name, dummyEmail, "", Locale.ENGLISH, null);
            userDao.get().insert(new UsersTable(authOrgUser.getId(), null, name, dummyEmail, null, List.of(), false, authOrgUser.getLang().toLanguageTag()));
        }

        // Just a note, the /api/sync_sso creates the org user here, so it will already be created when the above response is returned
        OrganizationsTable org = new OrganizationsTable(authOrgUser.getId(), name, ownerId, authOrgUser.getId());
        org = organizationDao.get().insert(org);
        long orgId = org.getId();
        UserData orgUser = userService.getUserData(authOrgUser.getId());
        roleService.addGlobalRole(orgUser.getUser().getId(), Role.ORGANIZATION.getRoleId());
        roleService.addOrgMemberRole(orgId, ownerId, Role.ORGANIZATION_OWNER, true);
        List<String> newState = new ArrayList<>();
        // TODO user action logging for org members
        members.forEach((memberId, role) -> {
            UsersTable memberUser = userDao.get().getById(memberId);
            newState.add(memberUser.getName() + ": " + role.getTitle());
            roleService.addOrgMemberRole(orgId, memberId, role, false);
            notificationService.sendNotification(memberId, orgId, NotificationType.ORGANIZATION_INVITE, new String[]{"notification.organization.invite", role.getTitle(), name});
        });
        userActionLogService.organization(request, LoggedActionType.ORG_MEMBERS_ADDED.with(OrganizationContext.of(orgId)), String.join("<br>", newState), "<i>No Members</i>");
        return org;
    }
}
