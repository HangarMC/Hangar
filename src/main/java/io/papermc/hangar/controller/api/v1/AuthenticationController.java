package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.api.v1.interfaces.IAuthenticationController;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.api.requests.SessionProperties;
import io.papermc.hangar.service.AuthenticationService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AuthenticationController extends HangarController implements IAuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // TODO JWT
    @Override
    public ResponseEntity<ApiSession> authenticate(SessionProperties body) {
        throw new NotImplementedException("Setup JWT here");
    }
}
