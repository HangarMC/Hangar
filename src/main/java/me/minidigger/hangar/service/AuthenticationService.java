package me.minidigger.hangar.service;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.generated.ApiSession;
import me.minidigger.hangar.model.generated.ApiSessionResponse;
import me.minidigger.hangar.model.generated.SessionProperties;
import me.minidigger.hangar.model.generated.SessionType;
import me.minidigger.hangar.security.HangarAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final HangarConfig hangarConfig;
    private final HangarDao<UserDao> userDao;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;

    @Autowired
    public AuthenticationService(HangarConfig hangarConfig, HangarDao<UserDao> userDao, AuthenticationManager authenticationManager, RoleService roleService) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
    }

    public ApiSessionResponse authenticateDev() {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();
            OffsetDateTime sessionExpiration = OffsetDateTime.now().plus(hangarConfig.api.session.getExpiration());
            String uuidtoken = UUID.randomUUID().toString();
            ApiSession session = new ApiSession(uuidtoken, null, hangarConfig.fakeUser.getId(), sessionExpiration);

            saveSession(session);

            return new ApiSessionResponse(session.getToken(), session.getExpires(), SessionType.DEV);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public ApiSessionResponse authenticateKeyPublic(SessionProperties properties, long userId) {
        // TODO, get properties session expiration and stuff
        OffsetDateTime sessionExpiration = OffsetDateTime.now().plus(hangarConfig.api.session.getExpiration());
        String uuidtoken = UUID.randomUUID().toString();
        ApiSession session = new ApiSession(uuidtoken, null, userId, sessionExpiration);

        saveSession(session);

        return new ApiSessionResponse(session.getToken(), session.getExpires(), SessionType.USER);
    }

    public ApiSessionResponse authenticateUser(long userId) {
        OffsetDateTime sessionExpiration = OffsetDateTime.now().plus(hangarConfig.api.session.getExpiration());
        String uuidtoken = UUID.randomUUID().toString();
        ApiSession session = new ApiSession(uuidtoken, null, userId, sessionExpiration);

        saveSession(session);

        return new ApiSessionResponse(session.getToken(), session.getExpires(), SessionType.USER);
    }

    private void saveSession(ApiSession session) {

    }

    public void loginAsFakeUser() {
        String username = hangarConfig.fakeUser.getUsername();
        UsersTable userEntry = userDao.get().getByName(username);
        System.out.println(hangarConfig.channels.getColorDefault());
        if (userEntry == null) {
            userEntry = new UsersTable();
            userEntry.setEmail(hangarConfig.fakeUser.getEmail());
            userEntry.setFullName(hangarConfig.fakeUser.getName());
            userEntry.setName(hangarConfig.fakeUser.getUsername());
            userEntry.setId(hangarConfig.fakeUser.getId());
            userEntry.setReadPrompts(new int[0]);

            userEntry = userDao.get().insert(userEntry);

            roleService.addGlobalRole(userEntry.getId(), Role.HANGAR_ADMIN.getRoleId());
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
