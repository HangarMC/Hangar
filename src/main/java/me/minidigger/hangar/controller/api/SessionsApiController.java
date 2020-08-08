package me.minidigger.hangar.controller.api;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.api.SessionsDao;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.service.AuthenticationService.AuthCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class SessionsApiController implements SessionsApi {

    private static final Logger log = LoggerFactory.getLogger(SessionsApiController.class);

    private final HangarDao<SessionsDao> sessionsDao;
    private final AuthenticationService authenticationService;

    @Autowired
    public SessionsApiController(HangarDao<SessionsDao> sessionsDao, AuthenticationService authenticationService) {
        this.sessionsDao = sessionsDao;
        this.authenticationService = authenticationService;
    }

    @PreAuthorize("@authenticationService.apiAction(T(me.minidigger.hangar.model.Permission).None, 'global')")
    @Override
    public ResponseEntity<Void> deleteSession() {
        AuthCredentials credentials = authenticationService.parseAuthHeader();
        if (credentials.getSession() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This request was not made with a session");
        }
        sessionsDao.get().delete(credentials.getSession());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
