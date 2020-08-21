package io.papermc.hangar.controller.api;

import io.papermc.hangar.model.SsoSyncData;
import io.papermc.hangar.service.SsoService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.config.hangar.SsoConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Controller
public class SsoApiController implements SsoApi {

    private final UserService userService;
    private final SsoService signer;
    private final SsoConfig ssoConfig;

    private static final Logger log = LoggerFactory.getLogger(SsoApiController.class);

    @Autowired
    public SsoApiController(UserService userService, SsoService signer, SsoConfig ssoConfig) {
        this.userService = userService;
        this.signer = signer;
        this.ssoConfig = ssoConfig;
    }

    @Override
    public ResponseEntity<Void> syncSso(@NotEmpty String sso, @NotEmpty String sig, @RequestParam(name = "api_key") @NotEmpty String apiKey) {
        if (!apiKey.equals(ssoConfig.getApiKey())) {
            log.warn("SSO sync failed: bad API key (" + apiKey + " provided, " + ssoConfig.getApiKey() + " expected)");
            return ResponseEntity.badRequest().build();
        }

        try {
            Map<String, String> map = signer.decode(sso, sig);
            SsoSyncData data = SsoSyncData.fromSignedPayload(map);
            userService.ssoSyncUser(data);
            log.debug("SSO sync successful: " + map.toString());
            return ResponseEntity.ok().build();
        } catch (SsoService.SignatureException e) {
            log.warn("SSO sync failed: invalid signature (" + sig + " for data " + sso + ")");
            return ResponseEntity.badRequest().build();
        }
    }
}
