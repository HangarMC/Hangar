package io.papermc.hangar.controller.api;

public class RefreshResponse {

    private final String token;
    private final String refreshToken;
    private final String cookieName;

    public RefreshResponse(String token, String refreshToken, String cookieName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.cookieName = cookieName;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getCookieName() {
        return cookieName;
    }
}
