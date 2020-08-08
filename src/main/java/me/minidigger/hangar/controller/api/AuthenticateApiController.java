package me.minidigger.hangar.controller.api;

import me.minidigger.hangar.model.generated.ApiSessionResponse;
import me.minidigger.hangar.model.generated.SessionProperties;
import me.minidigger.hangar.security.HangarAuthentication;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class AuthenticateApiController implements AuthenticateApi {

    private final AuthenticationService service;

    @Autowired
    public AuthenticateApiController(AuthenticationService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ApiSessionResponse> authenticate(SessionProperties body) {
        if (body != null && body.isFake()) {
            return ResponseEntity.ok(service.authenticateDev());
        } else {
            return ResponseEntity.ok(service.authenticateKeyPublic(body == null ? new SessionProperties() : body));
        }
            // TODO not sure if all this is needed because you don't need the hangarauth user id, all that is from the api key
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication instanceof HangarAuthentication) {
//                return ResponseEntity.ok(service.authenticateKeyPublic(body, ((HangarAuthentication) authentication).getUserId()));
//            } else if (authentication.getPrincipal().equals("anonymousUser")) {
//                return ResponseEntity.ok(service.authenticatePublic(body));
//            } else {
//                throw AuthUtils.unAuth();
//            }
//        }
    }

    @Override
    public ResponseEntity<ApiSessionResponse> authenticateUser(SessionProperties body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof HangarAuthentication) {
            return ResponseEntity.ok(service.authenticateUser(((HangarAuthentication) authentication).getUserId()));
        } else {
            throw AuthUtils.unAuth();
        }
    }
}
