package io.papermc.hangar.components.auth.model.credential;

// TODO webauthn credential
public record WebAuthNCredential(String userHandle, Object credentials) implements Credential{
    @Override
    public CredentialType type() {
        return CredentialType.WEBAUTHN;
    }
}
