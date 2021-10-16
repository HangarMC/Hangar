package io.papermc.hangar.model.internal.sso;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TokenResponce {

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

    public TokenResponce(String accessToken, long expiresIn, String idToken, String refreshToken, String scope, String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "TokenResponce{" +
               "accessToken='" + accessToken + '\'' +
               ", expiresIn=" + expiresIn +
               ", idToken='" + idToken + '\'' +
               ", refreshToken='" + refreshToken + '\'' +
               ", scope='" + scope + '\'' +
               ", tokenType='" + tokenType + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenResponce that = (TokenResponce) o;
        return expiresIn == that.expiresIn && Objects.equals(accessToken, that.accessToken) && Objects.equals(idToken, that.idToken) && Objects.equals(refreshToken, that.refreshToken) && Objects.equals(scope, that.scope) && Objects.equals(tokenType, that.tokenType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, expiresIn, idToken, refreshToken, scope, tokenType);
    }
}
