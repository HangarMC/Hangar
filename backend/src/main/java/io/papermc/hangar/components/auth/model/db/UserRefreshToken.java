package io.papermc.hangar.components.auth.model.db;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class UserRefreshToken extends Table {

    private final long userId;
    private OffsetDateTime lastUpdated;
    private UUID token;
    private final UUID deviceId;

    @JdbiConstructor
    public UserRefreshToken(final OffsetDateTime createdAt, final long id, final long userId, final OffsetDateTime lastUpdated, final UUID token, final UUID deviceId) {
        super(createdAt, id);
        this.userId = userId;
        this.lastUpdated = lastUpdated;
        this.token = token;
        this.deviceId = deviceId;
    }

    public UserRefreshToken(final long userId, final UUID token, final UUID deviceId) {
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
    }

    public long getUserId() {
        return this.userId;
    }

    public OffsetDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(final OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public UUID getToken() {
        return this.token;
    }

    public void setToken(final UUID token) {
        this.token = token;
    }

    public UUID getDeviceId() {
        return this.deviceId;
    }

    @Override
    public String toString() {
        return "UserRefreshToken{" +
            "userId=" + this.userId +
            ", lastUpdated=" + this.lastUpdated +
            ", token=" + this.token +
            ", deviceId=" + this.deviceId +
            "} " + super.toString();
    }
}
