package io.papermc.hangar.model.api.auth;

public class RefreshResponse {

    private final String token;
    private final String refreshToken;
    private final long expiresIn;
    private final String cookieName;

    public RefreshResponse(final String token, final String refreshToken, final long expiresIn, final String cookieName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.cookieName = cookieName;
    }

    public String getToken() {
        return this.token;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public long getExpiresIn() {
        return this.expiresIn;
    }

    public String getCookieName() {
        return this.cookieName;
    }

    @Override
    public String toString() {
        return "RefreshResponse{" +
            "token='" + this.token + '\'' +
            ", refreshToken='" + this.refreshToken + '\'' +
            ", expiresIn=" + this.expiresIn +
            ", cookieName='" + this.cookieName + '\'' +
            '}';
    }
}
