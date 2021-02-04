package io.papermc.hangar.controller.api;

import java.time.Duration;

public class RefreshResponse {

    private final String token;
    private final String refreshToken;
    private final long expiresIn;
    private final String cookieName;

    public RefreshResponse(String token, String refreshToken, Duration expiresIn, String cookieName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn.getSeconds();
        this.cookieName = cookieName;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getCookieName() {
        return cookieName;
    }
}
