package io.papermc.hangar.model.internal.sso;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private long expiresIn;
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;

    public TokenResponse(final String accessToken, final long expiresIn, final String idToken, final String refreshToken, final String scope, final String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return this.expiresIn;
    }

    public void setExpiresIn(final long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getIdToken() {
        return this.idToken;
    }

    public void setIdToken(final String idToken) {
        this.idToken = idToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public void setTokenType(final String tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "TokenResponce{" +
               "accessToken='" + this.accessToken + '\'' +
               ", expiresIn=" + this.expiresIn +
               ", idToken='" + this.idToken + '\'' +
               ", refreshToken='" + this.refreshToken + '\'' +
               ", scope='" + this.scope + '\'' +
               ", tokenType='" + this.tokenType + '\'' +
               '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final TokenResponse that = (TokenResponse) o;
        return this.expiresIn == that.expiresIn && Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.idToken, that.idToken) && Objects.equals(this.refreshToken, that.refreshToken) && Objects.equals(this.scope, that.scope) && Objects.equals(this.tokenType, that.tokenType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.accessToken, this.expiresIn, this.idToken, this.refreshToken, this.scope, this.tokenType);
    }
}
