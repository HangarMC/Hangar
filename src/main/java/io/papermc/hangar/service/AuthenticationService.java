package io.papermc.hangar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.ChangeAvatarToken;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthenticationService extends HangarComponent {

    private final UserService userService;
    private final GlobalRoleService globalRoleService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public AuthenticationService(UserService userService, GlobalRoleService globalRoleService, RestTemplate restTemplate, ObjectMapper mapper) {
        this.userService = userService;
        this.globalRoleService = globalRoleService;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    public UserTable loginAsFakeUser() {
        String userName = config.fakeUser.getUsername();
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            userTable = new UserTable(
                    -1, // we can pass -1 here since it's not actually inserted in the DB in the DAO
                    UUID.randomUUID(),
                    config.fakeUser.getName(),
                    userName,
                    config.fakeUser.getEmail(),
                    List.of(),
                    false,
                    Locale.ENGLISH.toLanguageTag()
            );

            userTable = userService.insertUser(userTable);

            globalRoleService.addRole(GlobalRole.HANGAR_ADMIN.create(null, userTable.getId(), true));
        }
        return userTable;
    }

    public URI changeAvatarUri(String requester, String organization) throws JsonProcessingException {
        ChangeAvatarToken token = getChangeAvatarToken(requester, organization);
        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromHttpUrl(config.sso.getAuthUrl());
        uriComponents.path("/accounts/user/{organization}/change-avatar/").queryParam("key", token.getSignedData());
        return uriComponents.build(organization);
    }

    public ChangeAvatarToken getChangeAvatarToken(String requester, String organization) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        // TODO allow changing org avatars in SSO
        if (true) throw new RuntimeException("disabled");
//        bodyMap.add("api-key", config.sso.getApiKey());
        bodyMap.add("request_username", requester);
        ChangeAvatarToken token;
        token = mapper.treeToValue(restTemplate.postForObject(config.security.api.getUrl() + "/api/users/" + organization + "/change-avatar-token/", new HttpEntity<>(bodyMap, headers), ObjectNode.class), ChangeAvatarToken.class);
        return token;
    }
}
