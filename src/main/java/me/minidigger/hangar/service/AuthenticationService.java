package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.model.ApiSession;
import me.minidigger.hangar.model.ApiSessionResponse;
import me.minidigger.hangar.model.SessionProperties;
import me.minidigger.hangar.model.SessionType;

@Service
public class AuthenticationService {

    private final HangarConfig hangarConfig;

    @Autowired
    public AuthenticationService(HangarConfig hangarConfig) {
        this.hangarConfig = hangarConfig;
    }

    public ApiSessionResponse authenticateDev() {
        if (hangarConfig.isFakeUserEnabled()) {
            hangarConfig.checkDebug();

            OffsetDateTime sessionExpiration = OffsetDateTime.now().plusSeconds(hangarConfig.getSessionExpiration());
            String uuidtoken = UUID.randomUUID().toString();
            ApiSession session = new ApiSession(uuidtoken, null, hangarConfig.getFakeUserId(), sessionExpiration);

            saveSession(session);

            return new ApiSessionResponse(session.getToken(), session.getExpires(), SessionType.DEV);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public ApiSessionResponse authenticateKeyPublic(SessionProperties properties, long userId) {
        // TODO, get properties session expiration and stuff
        OffsetDateTime sessionExpiration = OffsetDateTime.now().plusSeconds(hangarConfig.getSessionExpiration());
        String uuidtoken = UUID.randomUUID().toString();
        ApiSession session = new ApiSession(uuidtoken, null, userId, sessionExpiration);

        saveSession(session);

        return new ApiSessionResponse(session.getToken(), session.getExpires(), SessionType.USER);
    }

    public ApiSessionResponse authenticateUser(long userId) {
        OffsetDateTime sessionExpiration = OffsetDateTime.now().plusSeconds(hangarConfig.getSessionExpiration());
        String uuidtoken = UUID.randomUUID().toString();
        ApiSession session = new ApiSession(uuidtoken, null, userId, sessionExpiration);

        saveSession(session);

        return new ApiSessionResponse(session.getToken(), session.getExpires(), SessionType.USER);
    }

    private void saveSession(ApiSession session) {

    }
}
