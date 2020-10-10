package io.papermc.hangar.controller.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.SessionsDao;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.util.AuthUtils;
import io.papermc.hangar.util.AuthUtils.AuthCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class SessionsApiController implements SessionsApi {

    private static final Logger log = LoggerFactory.getLogger(SessionsApiController.class);

    private final HangarDao<SessionsDao> sessionsDao;

    @Autowired
    public SessionsApiController(HangarDao<SessionsDao> sessionsDao) {
        this.sessionsDao = sessionsDao;
    }

    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).None, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    @Override
    public ResponseEntity<Void> deleteSession() {
        AuthCredentials credentials = AuthUtils.parseAuthHeader(true);
        if (credentials.getSession() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "This request was not made with a session");
        }
        sessionsDao.get().delete(credentials.getSession());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
