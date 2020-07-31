package me.minidigger.hangar.controller.api;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.model.generated.SsoSyncSignedPayload;
import me.minidigger.hangar.util.DiscourseSsoSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.Map;

@Controller
public class SsoApiController implements SsoApi {

    private final DiscourseSsoSigner signer;
    private final HangarConfig.HangarSsoConfig ssoConfig;

    @Autowired
    public SsoApiController(DiscourseSsoSigner signer, HangarConfig.HangarSsoConfig ssoConfig) {
        this.signer = signer;
        this.ssoConfig = ssoConfig;
    }

    @Override
    public ResponseEntity<Void> syncSso(@Valid SsoSyncSignedPayload payload) {
        if (!payload.getApiKey().equals(ssoConfig.getApiKey())) {
            System.out.println("FAILED SSO SYNC: bad API key (provided " + payload.getApiKey() + " not " + ssoConfig.getApiKey() + ")");
            return ResponseEntity.badRequest().build();
        }

        try {
            Map<String, String> map = signer.decode(payload.getSso(), payload.getSig());
            System.out.println("SUCCESSFUL SSO SYNC: " + map.toString());
            return ResponseEntity.ok().build();
        } catch (DiscourseSsoSigner.SignatureException e) {
            System.out.println("FAILED SSO SYNC: invalid signature (" + payload.getSig() + " for data " + payload.getSso() + ")");
            return ResponseEntity.badRequest().build();
        }
    }
}
