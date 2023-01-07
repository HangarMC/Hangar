package io.papermc.hangar.service.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.user.HangarUser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.web.server.ResponseStatusException;

@Service
public class AvatarService extends HangarComponent {

    public static final String USER = "user";
    public static final String PROJECT = "project";

    private final RestTemplate restTemplate;
    private final UserDAO userDAO;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).build();

    @Autowired
    public AvatarService(@Lazy final RestTemplate restTemplate, final UserDAO userDAO) {
        this.restTemplate = restTemplate;
        this.userDAO = userDAO;
    }

    /*
     * change methods
     */
    public void changeUserAvatar(final UUID uuid, final byte[] avatar) {
        this.changeAvatar(USER, uuid.toString(), avatar);
    }

    public void changeProjectAvatar(final long projectId, final byte[] avatar) {
        this.changeAvatar(PROJECT, projectId + "", avatar);
    }

    private void changeAvatar(final String type, final String subject, final byte[] avatar) {
        this.checkEnabled();

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new MultipartInputStreamFileResource(new ByteArrayInputStream(avatar), subject + ".webp"));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            final ResponseEntity<Void> response = this.restTemplate.postForEntity(this.config.security.api().url() + "/avatar/" + type + "/" + subject + "?apiKey=" + this.config.sso.apiKey(), requestEntity, Void.class);
            this.cache.invalidate(type + "-" + subject);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(response.getStatusCode(), "Error from auth api");
            }
        } catch (final HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }

    /*
     * Delete methods
     */
    public void deleteProjectAvatar(final long projectId) {
        this.deleteAvatar(PROJECT, projectId + "");
    }

    private void deleteAvatar(final String type, final String subject) {
        this.checkEnabled();

        try {
            this.restTemplate.delete(this.config.security.api().url() + "/avatar/" + type + "/" + subject + "?apiKey=" + this.config.sso.apiKey());
            this.cache.invalidate(type + "-" + subject);
        } catch (final HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }

    /*
     * Get methods
     */
    public String getUserAvatarUrl(final UserTable userTable) {
        return this.cache.get("user-" + userTable.getUuid().toString(), (key) ->
            this.getAvatarUrl(USER, userTable.getUuid().toString())
        );
    }

    public String getUserAvatarUrl(final User user) {
        return this.cache.get("user-" + user.getName(), (key) -> {
            if (user instanceof HangarUser hangarUser) {
                return this.getAvatarUrl(USER, hangarUser.getUuid().toString());
            }
            return this.getAvatarUrl(USER, this.userDAO.getUserTable(user.getName()).getUuid().toString());
        });
    }

    public String getOrgAvatar(final UUID orgUserUuid) {
        return this.cache.get("org-" + orgUserUuid.toString(), (key) ->
            this.getAvatarUrl(USER, orgUserUuid.toString())
        );
    }

    public String getProjectAvatarUrl(final long projectId, final String ownerName) {
        return this.cache.get("project-" + projectId, (key) -> {
            final UserTable userTable = this.userDAO.getUserTable(ownerName);
            if (userTable != null) {
                return this.getAvatarUrl(PROJECT, projectId + "", USER, userTable.getUuid().toString());
            } else {
                return this.getAvatarUrl(PROJECT, projectId + "");
            }
        });
    }

    private String getAvatarUrl(final String type, final String subject) {
        return this.getAvatarUrl(type, subject, null, null);
    }

    private String getAvatarUrl(final String type, final String subject, final String defaultType, final String defaultSubject) {
        if (this.config.fakeUser.enabled()) {
            return "https://docs.papermc.io/img/paper.png";
        }

        try {
            return this.restTemplate.getForObject(this.config.security.api().url() + "/avatar/" + type + "/" + subject + (defaultType != null && defaultSubject != null ? "/" + defaultType + "/" + defaultSubject : "") + "?apiKey=" + this.config.sso.apiKey(), String.class);
        } catch (final HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }

    private void checkEnabled() {
        if (this.config.fakeUser.enabled()) {
            throw new HangarApiException("Disabled since fake user is enabled!");
        }
    }

    // no clue why I need an InputStreamResource, ByteArrayResource ends in "Required part 'avatar' is not present." 🤷
    static class MultipartInputStreamFileResource extends InputStreamResource {

        private final String filename;

        MultipartInputStreamFileResource(final InputStream inputStream, final String filename) {
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
