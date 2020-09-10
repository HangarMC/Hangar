package io.papermc.hangar.controller.api;

import io.papermc.hangar.model.generated.ApiSessionResponse;
import io.papermc.hangar.model.generated.SessionProperties;
import io.papermc.hangar.security.HangarAuthentication;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@ApiController
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
            return ResponseEntity.ok(service.authenticateKeyPublic(body == null ? null : body.getExpiresIn()));
        }
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
