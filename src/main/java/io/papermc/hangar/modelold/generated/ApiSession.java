package io.papermc.hangar.modelold.generated;

import java.time.OffsetDateTime;

public class ApiSession {

    private String token;
    private ApiKey key;
    private Long user; // User?
    private OffsetDateTime expires;

    public ApiSession(String token, ApiKey key, Long user, OffsetDateTime expires) {
        this.token = token;
        this.key = key;
        this.user = user;
        this.expires = expires;
    }

    public String getToken() {
        return token;
    }

    public ApiKey getKey() {
        return key;
    }

    public Long getUser() {
        return user;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }
}
