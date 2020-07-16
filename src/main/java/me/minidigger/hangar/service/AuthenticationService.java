package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.generated.ApiSession;
import me.minidigger.hangar.model.generated.ApiSessionResponse;
import me.minidigger.hangar.model.generated.SessionProperties;
import me.minidigger.hangar.model.generated.SessionType;
import me.minidigger.hangar.security.HangarAuthentication;

@Service
public class AuthenticationService {

    private final HangarConfig hangarConfig;
    private final HangarDao<UserDao> userDao;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(HangarConfig hangarConfig, HangarDao<UserDao> userDao, AuthenticationManager authenticationManager) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
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

    public void loginAsFakeUser() {
        String username = hangarConfig.getFakeUserName();
        UsersTable userEntry = userDao.get().getByName(username);
        if (userEntry == null) {
            userEntry = new UsersTable();
            userEntry.setEmail(hangarConfig.getFakeUserEmail());
            userEntry.setFullName(hangarConfig.getFakeUserName());
            userEntry.setName(hangarConfig.getFakeUserUserName());
            userEntry.setId(hangarConfig.getFakeUserId());
            userEntry.setReadPrompts(new int[0]);

            userEntry = userDao.get().insert(userEntry);
        }
        // TODO properly do auth, remember me shit too
        Authentication auth = new HangarAuthentication(userEntry.getName());
        authenticationManager.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public boolean loginWithSSO(String sso, String sig) {
        return false;
    }
}
