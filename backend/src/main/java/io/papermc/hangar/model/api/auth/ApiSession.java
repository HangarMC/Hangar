package io.papermc.hangar.model.api.auth;

public class ApiSession {

    private final String token;
    private final long expiresIn;

    public ApiSession(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String toString() {
        return "ApiSession{" +
                "token='" + token + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
