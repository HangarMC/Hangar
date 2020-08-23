package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.OrganizationDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.viewhelpers.UserData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.service.sso.AuthUser;
import io.papermc.hangar.util.HangarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OrgFactory {

    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<UserDao> userDao;
    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final RoleService roleService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Autowired
    public OrgFactory(HangarDao<OrganizationDao> organizationDao, HangarDao<UserDao> userDao, HangarConfig hangarConfig, UserService userService, RoleService roleService, NotificationService notificationService, RestTemplate restTemplate, ObjectMapper mapper) {
        this.organizationDao = organizationDao;
        this.userDao = userDao;
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.roleService = roleService;
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    public OrganizationsTable createOrganization(String name, long ownerId, Map<Long, Role> members) {
        // TODO logging
        String dummyEmail = name.replaceAll("[^a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]", "") + '@' + hangarConfig.org.getDummyEmailDomain();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("api-key", hangarConfig.sso.getApiKey());
        map.add("username", name);
        map.add("email", dummyEmail);
        map.add("verified", Boolean.TRUE.toString());
        map.add("dummy", Boolean.TRUE.toString());
        AuthUser authOrgUser;
        try {
            authOrgUser = mapper.treeToValue(restTemplate.postForObject(hangarConfig.security.api.getUrl() + "/api/users", new HttpEntity<>(map, headers), ObjectNode.class), AuthUser.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HangarException("error.org.cannotCreate");
        }
        // TODO this will happen via /api/sync_sso, but I have no idea how to get that whole system working with Docker
        userDao.get().insert(new UsersTable(authOrgUser.getId(), null, name, dummyEmail, null, new int[0], false, authOrgUser.getLang().toLanguageTag()));
        OrganizationsTable org = new OrganizationsTable(name, ownerId, authOrgUser.getId());
        org = organizationDao.get().insert(org);
        long orgId = org.getId();
        UserData orgUser = userService.getUserData(authOrgUser.getId());
        roleService.addGlobalRole(orgUser.getUser().getId(), Role.ORGANIZATION.getRoleId());
        roleService.addOrgMemberRole(orgId, ownerId, Role.ORGANIZATION_OWNER, true);
        members.forEach((memberId, role) -> {
            roleService.addOrgMemberRole(orgId, memberId, role, false);
            notificationService.sendNotification(memberId, orgId, NotificationType.ORGANIZATION_INVITE, new String[]{"notification.organization.invite", role.getTitle(), name});
        });
        return org;
    }
}
