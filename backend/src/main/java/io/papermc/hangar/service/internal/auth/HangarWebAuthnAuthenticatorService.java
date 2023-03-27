package io.papermc.hangar.service.internal.auth;

import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticator;
import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticatorService;
import com.webauthn4j.springframework.security.exception.CredentialIdNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HangarWebAuthnAuthenticatorService implements WebAuthnAuthenticatorService {

    // TODO persist properly
    private final Map<String, WebAuthnAuthenticator> dum = new HashMap<>();

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
}
