package io.papermc.hangar.controller.internal;

import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticatorImpl;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.webauthn.model.AuthenticatorForm;
import io.papermc.hangar.service.internal.auth.HangarWebAuthnAuthenticatorService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RateLimit(path = "auth")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController extends HangarComponent {

    private final HangarWebAuthnAuthenticatorService hangarWebAuthnAuthenticatorService;

    public AuthController(final HangarWebAuthnAuthenticatorService hangarWebAuthnAuthenticatorService) {
        this.hangarWebAuthnAuthenticatorService = hangarWebAuthnAuthenticatorService;
    }

    @PostMapping("/auth/signup")
    public void signup() {

    }

    @PostMapping("/webauthn/register")
    @ResponseStatus(HttpStatus.OK)
    public void registerAuthenticator(@RequestBody final AuthenticatorForm form) {
        System.out.println("got form! " + form);
        final AttestationObject attestationObject = form.attestationObject().attestationObject();
        final Set<AuthenticatorTransport> transports;
        if (form.transports() != null) {
            transports = form.transports().stream().map(AuthenticatorTransport::create).collect(Collectors.toSet());
        } else {
            transports = Set.of();
        }
        final WebAuthnAuthenticatorImpl auth = new WebAuthnAuthenticatorImpl(form.name(), this.getHangarPrincipal(), attestationObject.getAuthenticatorData().getAttestedCredentialData(), attestationObject.getAttestationStatement(), 0, transports, null, null);
        this.hangarWebAuthnAuthenticatorService.store(this.getHangarPrincipal().getName(), auth);
    }
}
