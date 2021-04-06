package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.api.v1.interfaces.IAuthenticationController;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.service.api.APIAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AuthenticationController extends HangarController implements IAuthenticationController {

    private final APIAuthenticationService apiAuthenticationService;

    @Autowired
    public AuthenticationController(APIAuthenticationService apiAuthenticationService) {
        this.apiAuthenticationService = apiAuthenticationService;
    }

    @Anyone
    @Override
    public ResponseEntity<ApiSession> authenticate(String apiKey) {
        return ResponseEntity.ok(apiAuthenticationService.createJWTForApiKey(apiKey));
    }
}
