package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService extends HangarComponent {

    private final UserService userService;
    private final GlobalRoleService globalRoleService;
    private final RestTemplate restTemplate;

    public AuthenticationService(UserService userService, GlobalRoleService globalRoleService, RestTemplate restTemplate) {
        this.userService = userService;
        this.globalRoleService = globalRoleService;
        this.restTemplate = restTemplate;
    }

    public UserTable loginAsFakeUser() {
        String userName = config.fakeUser.username();
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            userTable = new UserTable(
                    -1, // we can pass -1 here since it's not actually inserted in the DB in the DAO
                    UUID.randomUUID(),
                    userName,
                    config.fakeUser.email(),
                    List.of(),
                    false,
                    Locale.ENGLISH.toLanguageTag(),
                    "white"
            );

            userTable = userService.insertUser(userTable);

            globalRoleService.addRole(GlobalRole.HANGAR_ADMIN.create(null, userTable.getId(), true));
        }
        return userTable;
    }

    public void changeAvatar(String org, MultipartFile avatar) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new MultipartInputStreamFileResource(avatar.getInputStream(), avatar.getOriginalFilename()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(config.security.api().url() + "/avatar/org/" + org + "?apiKey=" + config.sso.apiKey(), requestEntity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(response.getStatusCode(), "Error from auth api");
            }
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }

    static class MultipartInputStreamFileResource extends InputStreamResource {

        private final String filename;

        MultipartInputStreamFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() throws IOException {
            return -1; // we do not want to generally read the whole stream into memory ...
        }
    }
}
