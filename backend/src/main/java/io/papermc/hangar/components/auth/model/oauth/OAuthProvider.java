package io.papermc.hangar.components.auth.model.oauth;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public final class OAuthProvider {

    public enum Mode {
        GITHUB,
        OIDC
    }

    private final String name;
    private final String clientId;
    private final String clientSecret;
    private final String[] scopes;
    private final Mode mode;
    private final String wellKnown;
    private final String unlinkLink;

    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;

    public OAuthProvider(final String name, final String clientId, final String clientSecret, final String[] scopes, final Mode mode, final String wellKnown, String unlinkLink) {
        this.name = name;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scopes = scopes;
        this.mode = mode;
        this.wellKnown = wellKnown;
        this.unlinkLink = unlinkLink;
    }

    public String name() {
        return this.name;
    }

    public String clientId() {
        return this.clientId;
    }

    public String clientSecret() {
        return this.clientSecret;
    }

    public String[] scopes() {
        return this.scopes;
    }

    public Mode mode() {
        return this.mode;
    }

    public String wellKnown() {
        return this.wellKnown;
    }

    public String unlinkLink() {
        return this.unlinkLink;
    }

    public String authorizationEndpoint() {
        return this.authorizationEndpoint;
    }

    public void authorizationEndpoint(final String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String tokenEndpoint() {
        return this.tokenEndpoint;
    }

    public void tokenEndpoint(final String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String userInfoEndpoint() {
        return this.userInfoEndpoint;
    }

    public void userInfoEndpoint(final String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final var that = (OAuthProvider) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return "OAuthProvider[name=" + this.name + ']';
    }
}
