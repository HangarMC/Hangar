package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class UserRefreshToken extends Table {

    private final long userId;
    private final String token;

    @JdbiConstructor
    public UserRefreshToken(OffsetDateTime createdAt, long id, long userId, String token) {
        super(createdAt, id);
        this.userId = userId;
        this.token = token;
    }

    public UserRefreshToken(long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }
}
