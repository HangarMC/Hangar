package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.interfaces.IAuthenticationController;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.api.requests.SessionProperties;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.util.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class AuthenticationController extends HangarApiController implements IAuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public ResponseEntity<ApiSession> authenticate(SessionProperties body) {
        if (body != null && body.isFake()) {
            return ResponseEntity.ok(authenticationService.authenticateDev());
        } else {
            return ResponseEntity.ok(authenticationService.authenticateKeyPublic(body == null ? null : body.getExpiresIn()));
        }
    }

    @Override
    public ResponseEntity<ApiSession> authenticateUser(SessionProperties body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof HangarAuthenticationToken) {
            return ResponseEntity.ok(authenticationService.authenticateUser(((HangarAuthenticationToken) authentication).getUserId()));
        } else {
            throw AuthUtils.unAuth();
        }
    }
}
