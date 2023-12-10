package io.papermc.hangar.components.auth.model.credential;

public record OAuthCredential(String provider, String id, String name) implements Credential {

    @Override
    public CredentialType type() {
        return CredentialType.OAUTH;
    }
}
