package io.papermc.hangar.controllerold.api;

import io.papermc.hangar.config.hangar.SsoConfig;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.modelold.SsoSyncData;
import io.papermc.hangar.serviceold.SsoService;
import io.papermc.hangar.serviceold.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    public ResponseEntity<MultiValueMap<String, String>> syncSso(@NotEmpty String sso, @NotEmpty String sig, @RequestParam(name = "api_key") @NotEmpty String apiKey) {
        if (!apiKey.equals(ssoConfig.getApiKey())) {
            log.warn("SSO sync failed: bad API key (" + apiKey + " provided, " + ssoConfig.getApiKey() + " expected)");
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "SSO sync failed: bad API key (" + apiKey + " provided, " + ssoConfig.getApiKey() + " expected)");
        }

        try {
            Map<String, String> map = signer.decode(sso, sig);
            SsoSyncData data = SsoSyncData.fromSignedPayload(map);
            userService.ssoSyncUser(data);
            log.debug("SSO sync successful: " + map.toString());
            MultiValueMap<String, String> ssoResponse = new LinkedMultiValueMap<>();
            ssoResponse.set("status", "success");
            return ResponseEntity.ok(ssoResponse);
        } catch (SsoService.SignatureException e) {
            log.warn("SSO sync failed: invalid signature (" + sig + " for data " + sso + ")");
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "SSO sync failed: invalid signature (" + sig + " for data " + sso + ")");
        }
    }
}
