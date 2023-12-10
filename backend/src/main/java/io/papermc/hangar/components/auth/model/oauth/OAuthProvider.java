package io.papermc.hangar.components.auth.model.oauth;

public record OAuthProvider(String name, String clientId, String clientSecret, String[] scopes) {
}
