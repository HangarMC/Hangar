package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserRefreshToken extends Table {

    private final long userId;
    private OffsetDateTime lastUpdated;
    private UUID token;
    private final UUID deviceId;

    @JdbiConstructor
    public UserRefreshToken(OffsetDateTime createdAt, long id, long userId, OffsetDateTime lastUpdated, UUID token, UUID deviceId) {
        super(createdAt, id);
        this.userId = userId;
        this.lastUpdated = lastUpdated;
        this.token = token;
        this.deviceId = deviceId;
    }

    public UserRefreshToken(long userId, UUID token, UUID deviceId) {
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
    }

    public long getUserId() {
        return userId;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        return "UserRefreshToken{" +
                "userId=" + userId +
                ", lastUpdated=" + lastUpdated +
                ", token=" + token +
                ", deviceId=" + deviceId +
                "} " + super.toString();
    }
}
