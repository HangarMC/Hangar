package io.papermc.hangar.components.auth.model.credential;

import java.util.List;

public record OAuthCredential(List<OAuthConnection> connections) implements Credential {

    public record OAuthConnection(String provider, String id, String name) {}

    @Override
    public CredentialType type() {
        return CredentialType.OAUTH;
    }
}
