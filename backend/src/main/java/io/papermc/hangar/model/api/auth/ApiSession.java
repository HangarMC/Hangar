package io.papermc.hangar.model.api.auth;

public class ApiSession {

    private final String token;
    private final long expiresIn;

    public ApiSession(final String token, final long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return this.token;
    }

    public long getExpiresIn() {
        return this.expiresIn;
    }

    @Override
    public String toString() {
        return "ApiSession{" +
                "token='" + this.token + '\'' +
                ", expiresIn=" + this.expiresIn +
                '}';
    }
}
