package io.papermc.hangar.components.auth.service;

import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticator;
import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticatorService;
import com.webauthn4j.springframework.security.exception.CredentialIdNotFoundException;
import com.webauthn4j.springframework.security.options.PublicKeyCredentialUserEntityProvider;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class WebAuthNService implements WebAuthnAuthenticatorService, PublicKeyCredentialUserEntityProvider {

    private final AuthService authService;

    // TODO persist properly
    private final Map<String, WebAuthnAuthenticator> dum = new HashMap<>();

    public WebAuthNService(final AuthService authService) {
        this.authService = authService;
    }

    public void store(final String name, final WebAuthnAuthenticator auth) {
        System.out.println("store " + name + " " + auth);
        this.dum.put(name, auth);
    }

    @Override
    public void updateCounter(final byte[] credentialId, final long counter) throws CredentialIdNotFoundException {
        System.out.println("update counter " + Arrays.toString(credentialId) + " " + counter);
        final WebAuthnAuthenticator auth = this.getByCredId(credentialId);
        auth.setCounter(counter);
    }

    private WebAuthnAuthenticator getByCredId(final byte[] credId) {
        return this.dum.values().stream().filter(a -> Arrays.equals(a.getAttestedCredentialData().getCredentialId(), credId))
            .findFirst().orElseThrow(() -> new CredentialIdNotFoundException("not found"));
    }

    @Override
    public WebAuthnAuthenticator loadAuthenticatorByCredentialId(final byte[] credentialId) throws CredentialIdNotFoundException {
        final WebAuthnAuthenticator auth = this.getByCredId(credentialId);
        // TODO we need to make sure to load the proper hangar user principal here
        System.out.println("loadAuthenticatorByCredentialId " + Arrays.toString(credentialId) + " " + auth.getUserPrincipal());
        return auth;
    }

    @Override
    public List<WebAuthnAuthenticator> loadAuthenticatorsByUserPrincipal(final Object principal) {
        final WebAuthnAuthenticator e1 = this.dum.get(principal.toString());
        System.out.println("loadAuthenticatorsByUserPrincipal " + principal + " " + e1);
        if (e1 == null) {
            return List.of();
        }
        return List.of(e1);
    }

    @Override
    public PublicKeyCredentialUserEntity provide(final Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        System.out.println("load public key " + authentication);

        final String username = authentication.getName();
        final HangarPrincipal principal = this.authService.loadUserByUsername(username);
        return new PublicKeyCredentialUserEntity(
            BigInteger.valueOf(principal.getUserId()).toByteArray(), // TODO this isn't a good id I guess?
            principal.getUsername(),
            principal.getUsername()
        );
    }
}
