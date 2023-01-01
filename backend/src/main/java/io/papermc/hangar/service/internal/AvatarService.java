package io.papermc.hangar.service.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.user.HangarUser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
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

    private final RestTemplate restTemplate;
    private final UserDAO userDAO;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMillis(30)).build(); // TODO change back to minutes

    @Autowired
    public AvatarService(@Lazy final RestTemplate restTemplate, final UserDAO userDAO) {
        this.restTemplate = restTemplate;
        this.userDAO = userDAO;
    }

    public void changeAvatar(final String type, final String subject, final byte[] avatar) {
        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new MultipartInputStreamFileResource(new ByteArrayInputStream(avatar), subject + ".webp"));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            final ResponseEntity<Void> response = this.restTemplate.postForEntity(this.config.security.api().url() + "/avatar/"+ type +"/" + subject + "?apiKey=" + this.config.sso.apiKey(), requestEntity, Void.class);
            this.cache.invalidate(type + "-" + subject);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(response.getStatusCode(), "Error from auth api");
            }
        } catch (final HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }

    public void deleteAvatar(final String type, final String subject) {
        try {
            this.restTemplate.delete(this.config.security.api().url() + "/avatar/"+ type +"/" + subject + "?apiKey=" + this.config.sso.apiKey());
        } catch (final HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
        }
    }

    public String getAvatarUrl(final UserTable userTable) {
        return this.getAvatarUrl("user", userTable.getUuid().toString());
    }

    public String getAvatarUrl(final User user) {
        if (user instanceof HangarUser hangarUser) {
            return this.getAvatarUrl("user", hangarUser.getUuid().toString());
        }
        return this.getAvatarUrl("user", this.userDAO.getUserTable(user.getName()).getUuid().toString());
    }

    public String getAvatarUrl(final String type, final String subject) {
        return this.cache.get(type + "-" + subject, (key) -> {
            try {
                return this.restTemplate.getForObject(this.config.security.api().url() + "/avatar/" + type + "/" + subject + "?apiKey=" + this.config.sso.apiKey(), String.class);
            } catch (final HttpStatusCodeException ex) {
                throw new ResponseStatusException(ex.getStatusCode(), "Error from auth api: " + ex.getMessage(), ex);
            }
        });
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
