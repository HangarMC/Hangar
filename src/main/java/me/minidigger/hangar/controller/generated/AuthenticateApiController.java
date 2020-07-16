package me.minidigger.hangar.controller.generated;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import me.minidigger.hangar.security.HangarAuthentication;
import me.minidigger.hangar.model.generated.ApiSessionResponse;
import me.minidigger.hangar.model.generated.SessionProperties;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.util.AuthUtils;

@Controller
public class AuthenticateApiController implements AuthenticateApi {

    private final AuthenticationService service;

    @Autowired
    public AuthenticateApiController(AuthenticationService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<ApiSessionResponse> authenticate(SessionProperties body) {
        Boolean fake = body.isFake();
        if (fake != null && fake) {
            return ResponseEntity.ok(service.authenticateDev());
        } else {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof HangarAuthentication) {
                return ResponseEntity.ok(service.authenticateKeyPublic(body, ((HangarAuthentication) principal).getUserId()));
            } else {
                throw AuthUtils.unAuth();
            }
        }
    }

    @Override
    public ResponseEntity<ApiSessionResponse> authenticateUser(SessionProperties body) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof HangarAuthentication) {
            return ResponseEntity.ok(service.authenticateUser(((HangarAuthentication) principal).getUserId()));
        } else {
            throw AuthUtils.unAuth();
        }
    }
}
